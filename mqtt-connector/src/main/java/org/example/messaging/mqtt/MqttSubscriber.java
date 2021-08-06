package org.example.messaging.mqtt;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;


public class MqttSubscriber implements Subscriber<Message<?>> {

    private static Logger logger = Logger.getLogger(MqttSubscriber.class.getName());

    private final String server;
    private final int port;
    private final String topic;
    private final int qos;

    private final MqttClient client;

    private Subscription subscription;

    public MqttSubscriber(Config config) {
        server = config.getOptionalValue("server", String.class).orElse("localhost");
        logger.info("server: " + server);

        port = config.getOptionalValue("port", Integer.class).orElse(1883);
        logger.info("Port: " + port);

        topic = config.getValue("topic", String.class);
        logger.info("Topic: " + topic);

        qos = config.getOptionalValue("qos", Integer.class).orElse(1);
        logger.info("Qos: " + qos);

        client = MqttClient.create(Vertx.vertx());
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.fine("Subscribed.");

        client.connect(port, server, h -> {
            if(h.succeeded()){
                logger.info("mqtt connection established.");
            }else{
                throw new RuntimeException("Failed to establish mqtt connection - " + h.result().toString());
            }
        });

        (this.subscription = subscription).request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Message<?> message) {
        logger.fine("Doing onNext()");

        Object payload = message.getPayload();
        Buffer buffer = null;
        if(payload instanceof byte[]){
            buffer = Buffer.buffer((byte[])payload);
        }else if(payload instanceof String){
            buffer = Buffer.buffer((String)payload);
        }else if(payload instanceof ByteBuf){
            buffer = Buffer.buffer((ByteBuf)payload);
        }
        
        if(Objects.isNull(buffer)){
            subscription.cancel();
            logger.log(Level.SEVERE, "Unkown message type: " + payload.getClass().getName());
        }else{
            client.publish(topic, buffer, MqttQoS.valueOf(qos), false, false);
            message.ack();
        }
    }

    @Override
    public void onError(Throwable t) {
        client.disconnect().toCompletionStage().toCompletableFuture().join();
        logger.log(Level.SEVERE, "Error - " + t.getMessage(), t);
    }

    @Override
    public void onComplete() {
        client.disconnect().toCompletionStage().toCompletableFuture().join();
        logger.info("Completed.");
    }
}
