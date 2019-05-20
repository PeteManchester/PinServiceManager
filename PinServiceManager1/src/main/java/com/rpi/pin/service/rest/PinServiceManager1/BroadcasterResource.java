package com.rpi.pin.service.rest.PinServiceManager1;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.json.JSONArray;

@Singleton
@Path("broadcast")
public class BroadcasterResource {

    private SseBroadcaster broadcaster = new SseBroadcaster();

    @POST
    @Path("/save")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public String broadcastMessage(String message) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        String id = "" + Calendar.getInstance().getTimeInMillis();
        OutboundEvent event = eventBuilder.name("message")
            .mediaType(MediaType.TEXT_PLAIN_TYPE)
            .data(String.class, message)
            .id(id)
            .reconnectDelay(500)
            .build();
        saveToFile(message);
        System.out.println("is Reconnect Delay Set: "  + event.isReconnectDelaySet());
        broadcaster.broadcast(event);

        return "Message '" + message + "' has been broadcast.";
    }
    
    
	private void saveToFile(String s) {
		try {			
			FileWriter fw = new FileWriter("CloudPins.json");
			fw.write(s);
			fw.close();
		} catch (Exception e) {
			System.out.println("Error Saving to pins.json" + e);
		}
	}
	
    @GET
    @Path("/read")
    public String read() {
    	String content = "[]";
		try {
			content = new String(Files.readAllBytes(Paths.get("CloudPins.json")));
			JSONArray array = new JSONArray(content);
			return content;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
    }

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput listenToBroadcast() {
        final EventOutput eventOutput = new EventOutput();
        this.broadcaster.add(eventOutput);
        return eventOutput;
    }
}