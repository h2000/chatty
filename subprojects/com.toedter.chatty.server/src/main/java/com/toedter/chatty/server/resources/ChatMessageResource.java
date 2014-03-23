/**
 * Copyright (c) 2014 Kai Toedter
 * All rights reserved.
 * Licensed under MIT License, see http://toedter.mit-license.org/
 */

package com.toedter.chatty.server.resources;

import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.standard.StandardRepresentationFactory;
import com.toedter.chatty.model.ChatMessage;
import com.toedter.chatty.model.ChatMessageRepository;
import com.toedter.chatty.model.ModelFactory;
import com.toedter.chatty.model.User;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("messages")
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class},
        path = "atmos/messages",
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class ChatMessageResource {
    private static final RepresentationFactory representationFactory = new StandardRepresentationFactory();
    private static final MediaType HAL_JSON_TYPE = new MediaType("application", "hal+json");

    private final Logger logger = LoggerFactory.getLogger(ChatMessageResource.class);

    @GET
    @Produces(RepresentationFactory.HAL_JSON)
    public String getUsers(@Context UriInfo uriInfo) {
        String baseURI = uriInfo.getRequestUri().toString();

        Representation listRep = representationFactory.newRepresentation();
        listRep.withLink("self", baseURI, null, null, "en", "chatty");

        ChatMessageRepository chatMessageRepository = ModelFactory.getInstance().getChatMessageRepository();

        for (ChatMessage chatMessage : chatMessageRepository.getAll()) {
            Representation rep = representationFactory.newRepresentation();
            rep.withBean(chatMessage)
                    .withLink("self", baseURI + "/" + chatMessage.getId());
            listRep.withRepresentation("messages", rep);
        }
        return listRep.toString(RepresentationFactory.HAL_JSON);
    }

    @POST
    public void broadcast(String message) {
        logger.info("Got message in post: " + message);
        BroadcasterFactory.getDefault().lookup("/atmos/messages").broadcast(message);
    }

}
