package org.example.messaging.mqtt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

//import io.helidon.common.configurable.ScheduledThreadPoolSupplier;
//import io.helidon.common.configurable.ThreadPoolSupplier;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;

public class MqttPublisher implements Publisher<Message<?>> {

    private static Logger logger = Logger.getLogger(MqttPublisher.class.getName());
/*
    mp.messaging.connector.mqtt-connector.server=localhost
    mp.messaging.connector.mqtt-connector.port=1883
    mp.messaging.connector.mqtt-connector.topic=testtopic
    mp.messaging.connector.mqtt-connector.qos=1
*/
    private final String server;
    private final int port;
    private final String topic;
    private final int qos;
    private final String payloadType;
    private final String encoding;

    private final MqttClient client;

//    private final static ExecutorService executor = ThreadPoolSupplier
//        .builder().threadNamePrefix("mqtt-publisher-").build().get();
//    private final static ScheduledExecutorService se = ScheduledThreadPoolSupplier
//        .builder().threadNamePrefix("mqtt-health-").build().get();
    private final static ExecutorService executor = Executors.newCachedThreadPool();
    private final static ScheduledExecutorService se = Executors.newSingleThreadScheduledExecutor();

    @SuppressWarnings("unused")
    private Subscriber<? super Message<?>> subscriber;
    private MqttSubscription subscription;
    private boolean subscribed; // true after first subscribe

    public MqttPublisher(Config config) {
        server = config.getOptionalValue("server", String.class).orElse("localhost");
        logger.info("server: " + server);

        port = config.getOptionalValue("port", Integer.class).orElse(1883);
        logger.info("Port: " + port);

        topic = config.getValue("topic", String.class);
        logger.info("Topic: " + topic);

        qos = config.getOptionalValue("qos", Integer.class).orElse(1);
        logger.info("Qos: " + qos);

        payloadType = config.getOptionalValue("payload-type", String.class).orElse("bytearray");
        logger.info("Payload type: " + payloadType);

        encoding = config.getOptionalValue("encoding", String.class).orElse("UTF-8");
        if(payloadType.equalsIgnoreCase("string")){
            logger.info("Encoding: " + encoding);
        }

        client = MqttClient.create(Vertx.vertx());
    }

    @Override
    public synchronized void subscribe(Subscriber<? super Message<?>> subscriber) {
        if (subscribed)
            subscriber.onError(new IllegalStateException()); // only one subscriber is allowed to subscribe
        else {
            this.subscriber = subscriber;
            subscribed = true;
            subscription = new MqttSubscription(subscriber, executor);
            subscriber.onSubscribe(subscription);
            
            // check connection status every X seconds
            se.scheduleAtFixedRate(() -> {
                keepConnection();
            }, 0, 5, TimeUnit.SECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                stop();
            }));
        }
    }

    private boolean isConnecting = false;

    public synchronized void keepConnection(){
        if(client.isConnected() || isConnecting){
            return;
        }
        isConnecting = true;
        client.connect(port, server, h -> {
            if(h.succeeded()){
                logger.info("mqtt connection established.");
                client.subscribe(topic, qos);
            }else{
                logger.severe("Failed to establish mqtt connection.");
            }
            isConnecting = false;
        }).publishHandler(s -> {
            //System.out.println("RECEIVED!!");
            Buffer buffer = s.payload();
            String topic = s.topicName();
            int qos = s.qosLevel().value();

            if(payloadType.equalsIgnoreCase("string")){
                String message = buffer.toString("UTF-8");
                logger.fine(String.format("Received [%s %d]\n%s", topic, qos, message));
                subscription.submit(MqttMessage.of(message, topic, qos));
            }else{
                logger.fine(String.format("Received [%s %d]", topic, qos));
                subscription.submit(MqttMessage.of(buffer.getBytes(), topic, qos));
            }
        }).closeHandler(h -> {
            logger.info("mqtt connection closed.");
        });
    }

    public synchronized void stop(){
        se.shutdown();
        try {
            se.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) { logger.warning("Executer termination error: " + e.getMessage()); }

        client.disconnect().toCompletionStage().toCompletableFuture().join();
        subscription.close();

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) { logger.warning("Executer termination error: " + e.getMessage()); }
        logger.info("MqttPublisher was stopped.");
    }


    public static class MqttSubscription implements Subscription {
        private final Subscriber<? super Message<?>> subscriber;
        private final ExecutorService executor;
        private boolean completed;
        private long capacity = 0;

        MqttSubscription(Subscriber<? super Message<?>> subscriber, ExecutorService executor) {
            this.subscriber = subscriber;
            this.executor = executor;
        }

        public synchronized void close(){
            executor.execute(() -> subscriber.onComplete());
            completed = true;
        }

        public synchronized void submit(Message<?> item) {
            if(completed){
                throw new RuntimeException("Already completed.");
            }
            if(capacity <= 0){
                throw new RuntimeException("No Capacity right now.");
            }
            executor.submit(() -> {
                subscriber.onNext(item);
            });
            if(capacity != Long.MAX_VALUE) capacity--;
        }

        @Override
        public synchronized void request(long n) {
            logger.fine("request: " + n);
            if(n < 0) {
                executor.execute(() -> subscriber.onError(new IllegalArgumentException("Bad request: " + n)));
            }else{
                capacity = n;
            }
        }

        @Override
        public synchronized void cancel() {
            logger.info("cancel");
            completed = true;
        }
    }



}