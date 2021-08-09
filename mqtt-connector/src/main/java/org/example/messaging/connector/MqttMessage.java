package org.example.messaging.connector;

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

    private MqttMessage(T payload, String topic, Integer qos, Supplier<CompletionStage<Void>> ackSupplier){
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
        return new MqttMessage<>(payload, topic, qos, null);
    }

    public static <T> MqttMessage<T> of(T payload, Supplier<CompletionStage<Void>> ackSupplier){
        Objects.requireNonNull(payload);
        return new MqttMessage<>(payload, null, null, ackSupplier);
    }

    public static <T> MqttMessage<T> of(T payload, String topic, Integer qos, Supplier<CompletionStage<Void>> ackSupplier){
        Objects.requireNonNull(payload);
        return new MqttMessage<>(payload, topic, qos, ackSupplier);
    }

    public Optional<String> getTopic() {
        return Optional.ofNullable(topic);
    }

    public Optional<Integer> getQos() {
        return Optional.ofNullable(qos);
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