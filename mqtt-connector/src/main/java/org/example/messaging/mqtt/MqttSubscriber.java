package org.example.messaging.mqtt;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
        logger.info("port: " + port);

        topic = config.getValue("topic", String.class);
        logger.info("topic: " + topic);

        qos = config.getOptionalValue("qos", Integer.class).orElse(1);
        logger.info("qos: " + qos);

        client = MqttClient.create(Vertx.vertx());
    }

    private boolean isConnecting = false;

    private synchronized void connect(){
        if(client.isConnected() || isConnecting){
            return;
        }
        final CompletableFuture<Void> f = new CompletableFuture<>();
        isConnecting = true;
        client.connect(port, server, h -> {
            if(h.succeeded()){
                logger.info("mqtt connection established.");
            }else{
                logger.severe("Failed to establish mqtt connection.");
            }
            f.complete(null);
        });
        f.join();
        isConnecting = false;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.fine("Subscribed.");
        connect();
        (this.subscription = subscription).request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Message<?> message) {
        logger.fine("Doing onNext()");

        if(!client.isConnected()){
            connect();
        }

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
            String topic = this.topic;
            int qos = this.qos;
            if(message instanceof MqttMessage){
                MqttMessage<?> m = (MqttMessage)message;
                topic = m.getTopic().orElse(this.topic);
                qos = m.getQos().orElse(this.qos);
            }
            try{
                io.vertx.core.Future<Integer> future = client.publish(topic, buffer, MqttQoS.valueOf(qos), false, false);
                logger.fine(String.format("Publishing message [%s %d] %s", topic, qos, message));
                if(0 != qos){
                    future.onComplete(ar ->{
                        if(ar.succeeded()){
                            logger.fine("Message published: id=" + ar.result());
                            message.ack();
                        }else{
                            Throwable t = ar.cause();
                            String cause = Objects.isNull(t) ? "(no cause)" : t.getMessage();
                            logger.severe("Failed to publish message - " + cause);
                        }
                    });
                }else{
                    message.ack();
                }
            }catch(Exception e){
                logger.log(Level.SEVERE, "Failed to publish message: " + e.getMessage(), e);
            }
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
