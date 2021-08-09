package org.example.messaging.connector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;

public class MqttPublisher extends SubmissionPublisher<Message<?>> {

    private static Logger logger = Logger.getLogger(MqttPublisher.class.getName());

    private final String server;
    private final int port;
    private final String topic;
    private final int qos;
    private final String payloadType;
    private final String encoding;
    private final int bufferSize;
    private final long monitorInterval;
    private final boolean monitorPublisher;

    private final MqttClient client;

    private final static ExecutorService executor = Executors.newCachedThreadPool();
    private final static ScheduledExecutorService se = Executors.newSingleThreadScheduledExecutor();

    public MqttPublisher(Config config) {
        super(executor, config.getOptionalValue("buffer-size", Integer.class).orElse(Flow.defaultBufferSize()));

        server = config.getOptionalValue("server", String.class).orElse("localhost");
        logger.info("server: " + server);

        port = config.getOptionalValue("port", Integer.class).orElse(1883);
        logger.info("port: " + port);

        topic = config.getValue("topic", String.class);
        logger.info("topic: " + topic);

        qos = config.getOptionalValue("qos", Integer.class).orElse(1);
        logger.info("qos: " + qos);

        payloadType = config.getOptionalValue("payload-type", String.class).orElse("bytearray");
        logger.info("payload: " + payloadType);

        encoding = config.getOptionalValue("encoding", String.class).orElse("UTF-8");
        if(payloadType.equalsIgnoreCase("string")){
            logger.info("encoding: " + encoding);
        }

        bufferSize = config.getOptionalValue("buffer-size", Integer.class).orElse(Flow.defaultBufferSize());
        logger.info("buffer-size: " + bufferSize);

        monitorInterval = config.getOptionalValue("monitor-interval", Long.class).orElse(5000L);
        logger.info("monitor-interval: " + monitorInterval);

        monitorPublisher = config.getOptionalValue("monitor-publisher", Boolean.class).orElse(false);
        logger.info("monitor-lag: " + monitorPublisher);

        client = MqttClient.create(Vertx.vertx());

        se.scheduleAtFixedRate(() -> {
            keepConnection();
            if(monitorPublisher){
                String message = String.format("Estimated Maximum Lag=%d, Estimated Minimum Demand=%d.", this.estimateMaximumLag(), this.estimateMinimumDemand());
                logger.info(message);
            }
        }, 0, monitorInterval, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
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
                submit(MqttMessage.of(message, topic, qos));
            }else{
                logger.fine(String.format("Received [%s %d]", topic, qos));
                submit(MqttMessage.of(buffer.getBytes(), topic, qos));
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

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) { logger.warning("Executer termination error: " + e.getMessage()); }
        logger.info("MqttPublisher was stopped.");
    }


}