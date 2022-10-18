package it.piotrmachnik.gameroomservicespringwebsockets.controller;

import it.piotrmachnik.gameroomservicespringwebsockets.model.ChatMessage;
import it.piotrmachnik.gameroomservicespringwebsockets.model.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebsocketEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations sendingOperations;

    @EventListener
    public void handleWebSocketConnectListener(final SessionConnectedEvent event) {
        LOGGER.info("New connection to global chat!");
    }

    @EventListener
    public void handleSocketDisconnectListener(final SessionDisconnectEvent event) {
        final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        final String username = (String) headerAccessor.getSessionAttributes().get("username");
        final ChatMessage chatMessage = ChatMessage.builder().type(MessageType.DISCONNECT).sender(username).build();
        System.out.println("Disconnect: " + username);
        sendingOperations.convertAndSend("/topic/public", chatMessage);
    }
}
