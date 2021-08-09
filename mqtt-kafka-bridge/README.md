# MQTT -> Kafka Bridge

A simple message transfer application from MQTT to Kafka working with [Helidon MP and Kafka Connector](https://helidon.io/docs/v2/#/mp/reactivemessaging/04_kafka)  

Note that messages and keys are assumed to be a string type.

## (Optional Tools) REST POST -> MQTT & REST POST -> Kafka

Optionally you can post MQTT or Kafka messages via REST.

```
$ curl -X POST -H "Content-Type: application/json" -d '{"hello":"world!"}' localhost:8181/mqtt/publish
$ curl -X POST -H "Content-Type: application/json" -d '{"hello":"world!"}' localhost:8181/mqtt/publish?topic=sometopic&qos=1
```

```
$ curl -X POST -H "Content-Type: application/json" -d '{"hello":"world!"}' localhost:8181/kafka/publish
$ curl -X POST -H "Content-Type: application/json" -d '{"hello":"world!"}' localhost:8181/kafka/publish?key=somekey
```

## Settings

Parameters can be set via Java system properties or environment variables.

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
# don't change channel names: mqtt-sub, kafka-pub
mp.messaging.incoming.mqtt-sub.connector=mqtt-connector
mp.messaging.incoming.mqtt-sub.server=localhost
mp.messaging.incoming.mqtt-sub.port=1883
mp.messaging.incoming.mqtt-sub.topic=to-kafka
mp.messaging.incoming.mqtt-sub.qos=1
mp.messaging.incoming.mqtt-sub.payload-type=string

mp.messaging.outgoing.kafka-pub.connector=helidon-kafka
mp.messaging.outgoing.kafka-pub.topic=from-mqtt

# REST POST -> MQTT
# don't change the channel name: mqtt-post
mp.messaging.outgoing.mqtt-post.connector=mqtt-connector
mp.messaging.outgoing.mqtt-post.server=localhost
mp.messaging.outgoing.mqtt-post.port=1883
mp.messaging.outgoing.mqtt-post.topic=to-kafka
mp.messaging.outgoing.mqtt-post.qos=1

# REST POST → Kafka
# don't change the channel name: kafka-post
mp.messaging.outgoing.kafka-post.connector=helidon-kafka
mp.messaging.outgoing.kafka-post.topic=kafka-post
```