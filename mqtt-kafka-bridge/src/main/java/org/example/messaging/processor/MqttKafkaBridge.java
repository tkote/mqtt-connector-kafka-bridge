package org.example.messaging.processor;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.example.messaging.mqtt.MqttMessage;

import io.helidon.messaging.connectors.kafka.KafkaMessage;


@ApplicationScoped
public class MqttKafkaBridge {

    private static Logger logger = Logger.getLogger(MqttKafkaBridge.class.getName());

    @Incoming("mqtt-sub")
    @Outgoing("kafka-pub")
    public KafkaMessage<String, String> bridge(MqttMessage<String> message){
        logger.fine("Bridging... \n" + message.getPayload());
        return KafkaMessage.of(message.getTopic().orElse("n/a"), message.getPayload());
    }

}