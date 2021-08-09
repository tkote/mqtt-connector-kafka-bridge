package org.example.messaging.connector;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.reactivestreams.FlowAdapters;

@ApplicationScoped
@Connector("mqtt-connector")
public class MqttConnector implements IncomingConnectorFactory, OutgoingConnectorFactory {

    @Override 
    public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config config) {
        final MqttSubscriber subscriber = new MqttSubscriber(config);
        return ReactiveStreams.fromSubscriber(subscriber);
    }

    @Override 
    public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
        final MqttPublisher publisher = new MqttPublisher(config);
        return ReactiveStreams.fromPublisher(FlowAdapters.toPublisher(publisher));
    }
}