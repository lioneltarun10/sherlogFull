package com.xorcists.demo.chat.controller;

import com.xorcists.demo.chat.dto.ChatRequest;
import com.xorcists.demo.chat.dto.ChatResponse;
import com.xorcists.demo.chat.service.ChatService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    private static final Logger LOG = Logger.getLogger(ChatController.class);

    @Inject
    ChatService chatService;

    @POST
    @Path("/chat")
    public Response chat(ChatRequest request) {
        LOG.infof("Received chat request: %s", request.getMessage());
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ChatResponse("Message cannot be empty"))
                .build();
        }

        ChatResponse response = chatService.processMessage(request);
        LOG.infof("Sending chat response: %s", response.getReply());
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public Response health() {
        return Response.ok("OK").build();
    }
}
