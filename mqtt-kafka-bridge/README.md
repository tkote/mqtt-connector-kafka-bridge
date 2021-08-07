# MQTT -> Kafka Bridge

A simple message transfer application from MQTT to Kafka working with [Helidon MP and Kafka Connector](https://helidon.io/docs/v2/#/mp/reactivemessaging/04_kafka)

Optionally you can post MQTT message via REST.

```
curl -X POST -H "Content-Type: application/json" -d '{"hello":"world!"}' localhost:8181/mqtt/publish?topic=sometopic&qos=1
```