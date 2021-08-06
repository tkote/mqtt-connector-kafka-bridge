# MQTT Connector / MQTT-Kafka Bridge

1. MQTT Connector
2. MQTT-Kafka Bridge


### How to build

+ Build all

    ```
    $ mvn package
    ```

+ Create docker image of MQTT-Kafka Bridge

    ```
    $ mvn -f pom-docker.xml exec:exec@docker-build
    ```

+ Run docker image (example)

    ```
    $ docker run -d --rm --name mqtt-kafka-bridge \
      -e mp.messaging.connector.helidon-kafka.bootstrap.servers=kafka-broker:9092 \
      -e mp.messaging.connector.mqtt-connector.server=mqtt-broker \
      -e mp.messaging.incoming.mqtt-sub.topic=some-topic-on-mqtt \
      -e mp.messaging.outgoing.kafka-pub.topic=some-topic-on-kafka \
      mqtt-kafka-bridge
    ```