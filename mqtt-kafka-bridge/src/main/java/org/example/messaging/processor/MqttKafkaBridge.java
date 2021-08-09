package org.example.messaging.processor;

import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.example.messaging.connector.MqttMessage;

import io.helidon.messaging.connectors.kafka.KafkaMessage;


@ApplicationScoped
public class MqttKafkaBridge {

    private static Logger logger = Logger.getLogger(MqttKafkaBridge.class.getName());

    @Inject
    @ConfigProperty(name = "mqtt-kafka-bridge.kafka-key-as-mqtt-topic", defaultValue = "false")
    private boolean bKafkaKeyAsMqttTopic;

    public MqttKafkaBridge(){
        logger.info("kafka-key-as-mqtt-topic: " + bKafkaKeyAsMqttTopic);
    }

    @Incoming("mqtt-sub")
    @Outgoing("kafka-pub")
    public KafkaMessage<String, String> bridge(MqttMessage<String> message){
        String mqttPayload = message.getPayload();
        logger.fine("Bridging... \n" + message.getPayload());
        Optional<String> mqttTopic = message.getTopic();
        KafkaMessage<String, String> kafkaMessage
            = (mqttTopic.isPresent() && bKafkaKeyAsMqttTopic) 
                ? KafkaMessage.of(mqttTopic.get(), mqttPayload) : KafkaMessage.of(mqttPayload);
        return kafkaMessage;
    }

}