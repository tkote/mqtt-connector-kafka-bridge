package org.example.messaging.processor;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import io.helidon.messaging.connectors.kafka.KafkaMessage;

@Path("/kafka")
@ApplicationScoped
public class KafkaPostResource {

    private final SubmissionPublisher<KafkaMessage<String, String>> publisher = new SubmissionPublisher<>(
        Executors.newCachedThreadPool(), Flow.defaultBufferSize()
    );

    @POST @Path("/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submitPost(String message, @QueryParam("key") String key) {
        Objects.requireNonNull(message);
        KafkaMessage<String,String> payload = 
            Objects.isNull(key) ? KafkaMessage.of(message) : KafkaMessage.of(key, message);
        publisher.submit(payload);
        return Response.status(Status.ACCEPTED).build();
    }

    @Outgoing("kafka-post")
    public Publisher<KafkaMessage<String, String>> preparePublisher() {
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }    

}
