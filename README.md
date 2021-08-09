# MQTT Connector / MQTT-Kafka Bridge

1. [MQTT Connector](mqtt-connector)
2. [MQTT-Kafka Bridge](mqtt-kafka-bridge)


### How to build

+ Build all

    ```
    $ mvn package
    ```

+ Create a docker image of MQTT-Kafka Bridge

    ```
    $ mvn -f pom-docker.xml exec:exec@docker-build
    ```

+ Run a docker image (example)

    ```
    $ docker run -d --rm --name mqtt-kafka-bridge \
      -e mp.messaging.connector.helidon-kafka.bootstrap.servers=kafka-broker:9092 \
      -e mp.messaging.incoming.mqtt-sub.server=mqtt-broker \
      -e mp.messaging.incoming.mqtt-sub.topic=some-topic-on-mqtt \
      -e mp.messaging.outgoing.kafka-pub.topic=some-topic-on-kafka \
      -e mp.messaging.outgoing.mqtt-post.topic=another-topic-on-mqtt \
      -e mp.messaging.outgoing.kafka-post.connector=null \
      -v $(pwd)/logging-debug.properties:/helidon/logging-debug.properties \
      -p 8181:8181 \
      mqtt-kafka-bridge java \
      -Djava.util.logging.config.file=logging-debug.properties \
      -jar mqtt-kafka-bridge.jar
    ```
