# 1st stage, build the app
FROM maven:3.6.3-jdk-11 as build

WORKDIR /helidon
RUN mkdir -p /helidon/mqtt-connector/target/classes # for weaving
RUN mkdir -p /helidon/mqtt-kafka-bridge/target/classes # for weaving

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD mqtt-connector/pom.xml ./mqtt-connector
ADD mqtt-kafka-bridge/pom.xml ./mqtt-kafka-bridge
ADD pom.xml .
RUN mvn package -DskipTests

# Do the Maven build!
# Incremental docker builds will resume here when you change sources
ADD mqtt-connector/src ./mqtt-connector/src
ADD mqtt-kafka-bridge/src ./mqtt-kafka-bridge/src
RUN mvn package -DskipTests
RUN echo "done!"

# 2nd stage, build the runtime image
FROM adoptopenjdk:11-jre-hotspot
WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY --from=build /helidon/mqtt-kafka-bridge/target/mqtt-kafka-bridge.jar ./
COPY --from=build /helidon/mqtt-kafka-bridge/target/libs ./libs

CMD ["java", "-jar", "mqtt-kafka-bridge.jar"]

EXPOSE 8181
