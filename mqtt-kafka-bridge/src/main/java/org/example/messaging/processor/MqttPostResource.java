package org.example.messaging.processor;

import java.util.Objects;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.example.messaging.connector.MqttMessage;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

@Path("/mqtt")
@ApplicationScoped
public class MqttPostResource {

    private static Logger logger = Logger.getLogger(MqttPostResource.class.getName());

    private final SubmissionPublisher<MqttMessage<String>> publisher;
    private final boolean enabled;

    @Inject
    public MqttPostResource(
        @ConfigProperty(name = "mp.messaging.outgoing.mqtt-post.connector", defaultValue = "null")
        String connector
    ){
        logger.info("connector: " + connector);
        enabled = connector.equals("null") ? false : true;
        logger.info("enabled: " + enabled);
        publisher = new SubmissionPublisher<>();
    }

    @POST @Path("/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submitPost(String message, @QueryParam("topic") String topic, @QueryParam("qos") Integer qos) {
        if(!enabled){
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
        Objects.requireNonNull(message);
        publisher.submit(MqttMessage.of(message, topic, qos));
        return Response.status(Status.ACCEPTED).build();
    }

    @Outgoing("mqtt-post")
    public Publisher<MqttMessage<String>> preparePublisher() {
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }    

}
