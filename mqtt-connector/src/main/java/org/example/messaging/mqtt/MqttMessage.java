package org.example.messaging.mqtt;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.eclipse.microprofile.reactive.messaging.Message;

public class MqttMessage<T> implements Message<T>{
    private static Logger logger = Logger.getLogger(MqttMessage.class.getName());

    private final T payload;
    private final Supplier<CompletionStage<Void>> ackSupplier;

    private final String topic;
    private final Integer qos;

    public MqttMessage(T payload, Supplier<CompletionStage<Void>> ackSupplier, String topic, Integer qos){
        this.payload = payload;
        this.ackSupplier = Optional.ofNullable(ackSupplier).orElse(() -> {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.complete(null);
            return f;
        });
        this.topic = topic;
        this.qos = qos;
    }

    public static <T> MqttMessage<T> of(T payload){
        Objects.requireNonNull(payload);
        return new MqttMessage<>(payload, null, null, null);
    }

    public static <T> MqttMessage<T> of(T payload, String topic, Integer qos){
        Objects.requireNonNull(payload);
        return new MqttMessage<>(payload, null, topic, qos);
    }

    public static <T> MqttMessage<T> of(T payload, Supplier<CompletionStage<Void>> ackSupplier){
        Objects.requireNonNull(payload);
        return new MqttMessage<>(payload, ackSupplier, null, null);
    }

    public static <T> MqttMessage<T> of(T payload, Supplier<CompletionStage<Void>> ackSupplier, String topic, Integer qos){
        Objects.requireNonNull(payload);
        return new MqttMessage<>(payload, ackSupplier, topic, qos);
    }

    public Optional<String> getTopic() {
        return Optional.of(topic);
    }

    public Optional<Integer> getQos() {
        return Optional.of(qos);
    }

    @Override
    public CompletionStage<Void> ack(){
        logger.fine("ack(): " + this);
        return ackSupplier.get();
    }

	@Override
	public T getPayload() {
		return payload;
    }


}