package it.piotrmachnik.gameRoomService.controller;

import it.piotrmachnik.gameRoomService.model.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/chat.send/{gameRoomId}")
    public void sendMessage(@Payload final ChatMessage chatMessage, @DestinationVariable String gameRoomId) {
        this.simpMessagingTemplate.convertAndSend("/topic/private/chat/" + gameRoomId, chatMessage);
    }

    @MessageMapping("/chat.newPlayer/{gameRoomId}")
    public void newPlayerPrivateChat(@Payload final ChatMessage chatMessage, @DestinationVariable String gameRoomId, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("playerId", chatMessage.getSenderId());
        this.simpMessagingTemplate.convertAndSend("/topic/private/chat/" + gameRoomId, chatMessage);
    }
}
