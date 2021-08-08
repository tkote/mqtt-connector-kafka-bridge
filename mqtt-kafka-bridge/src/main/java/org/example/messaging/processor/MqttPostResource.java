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
import org.example.messaging.mqtt.MqttMessage;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

@Path("/mqtt")
@ApplicationScoped
public class MqttPostResource {

    private final SubmissionPublisher<MqttMessage<String>> publisher = new SubmissionPublisher<>(
        Executors.newCachedThreadPool(), Flow.defaultBufferSize()
    );

    @POST @Path("/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submitPost(String message, @QueryParam("topic") String topic, @QueryParam("qos") Integer qos) {
        Objects.requireNonNull(message);
        publisher.submit(MqttMessage.of(message, topic, qos));
        return Response.status(Status.ACCEPTED).build();
    }

    @Outgoing("mqtt-pub")
    public Publisher<MqttMessage<String>> preparePublisher() {
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }    

}
