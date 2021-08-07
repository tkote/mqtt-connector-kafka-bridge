# MQTT -> Kafka Bridge

A simple message transfer application from MQTT to Kafka working with [Helidon MP and Kafka Connector](https://helidon.io/docs/v2/#/mp/reactivemessaging/04_kafka)

## REST POST -> MQTT Tool

Optionally you can post MQTT message via REST.

```
curl -X POST -H "Content-Type: application/json" -d '{"hello":"world!"}' localhost:8181/mqtt/publish?topic=sometopic&qos=1
```

## Setting

Parameters can be set via environment variables.

```
# Microprofile server properties
server.port=8181
server.host=0.0.0.0

# Kafka Connector
mp.messaging.connector.helidon-kafka.bootstrap.servers=localhost:9092
mp.messaging.connector.helidon-kafka.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.connector.helidon-kafka.value.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.connector.helidon-kafka.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.connector.helidon-kafka.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer

# MQTT -> Kafka Bridge
mp.messaging.incoming.mqtt-sub.connector=mqtt-connector
mp.messaging.incoming.mqtt-sub.server=localhost
mp.messaging.incoming.mqtt-sub.port=1883
mp.messaging.incoming.mqtt-sub.topic=to-kafka
mp.messaging.incoming.mqtt-sub.qos=1
mp.messaging.incoming.mqtt-sub.payload-type=string

mp.messaging.outgoing.kafka-pub.connector=helidon-kafka
mp.messaging.outgoing.kafka-pub.topic=from-mqtt

# REST POST -> MQTT
mp.messaging.outgoing.mqtt-pub.connector=mqtt-connector
mp.messaging.outgoing.mqtt-pub.server=localhost
mp.messaging.outgoing.mqtt-pub.port=1883
mp.messaging.outgoing.mqtt-pub.topic=to-kafka
mp.messaging.outgoing.mqtt-pub.qos=1
```