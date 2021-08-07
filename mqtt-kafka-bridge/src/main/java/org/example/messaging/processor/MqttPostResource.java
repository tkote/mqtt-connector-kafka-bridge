package org.example.messaging.processor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
//import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    //private final static Logger logger = Logger.getLogger(KafkaResource.class.getSimpleName());

    //@Inject
    //private KafkaPublisher publisher;
    private final SubmissionPublisher<MqttMessage<String>> publisher = new SubmissionPublisher<>(
        Executors.newCachedThreadPool(), Flow.defaultBufferSize()
    );

    /**
     * curl -X POST -H "Content-type: application/json" -d '["Ochacafe #1","Ochacafe #2","Ochacafe #3","Ochacafe #4","Ochacafe #5","Ochacafe #6","Ochacafe #7","Ochacafe #8"]' localhost:8080/kafka/publish
     */
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
