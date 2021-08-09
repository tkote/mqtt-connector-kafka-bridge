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
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@ApplicationScoped
@Connector("null")
public class NullConnector implements IncomingConnectorFactory, OutgoingConnectorFactory {

    @Override 
    public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config config) {
        return ReactiveStreams.fromSubscriber(new NullSubscriber());
    }

    @Override 
    public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
        return ReactiveStreams.fromPublisher(new NullPublisher());
    }

    public class NullPublisher implements Publisher<Message<?>>{

        @Override
        public void subscribe(Subscriber<? super Message<?>> s) {}
        
    }

    public class NullSubscriber implements Subscriber<Message<?>>{

        //@SuppressWarnings("unused")
        //private Subscription subscription;

        @Override
        public void onComplete() {}

        @Override
        public void onError(Throwable t) {}

        @Override
        public void onNext(Message<?> t) {}

        @Override
        public void onSubscribe(Subscription subscription) {
            //(this.subscription = subscription).request(Long.MAX_VALUE);
        }
        
    }


}