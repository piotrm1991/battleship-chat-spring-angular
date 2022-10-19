package it.piotrmachnik.gameRoomService.controller;

import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoomResponse;
import it.piotrmachnik.gameRoomService.model.lobby.LobbyMessage;
import it.piotrmachnik.gameRoomService.service.GameRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin(origins = "http://localhost:8083/")
public class LobbyController {

    @Autowired
    private GameRoomService gameRoomService;

    @MessageMapping("/lobby.newPlayer")
    @SendTo("/topic/public")
    public LobbyMessage newPlayer(@Payload final LobbyMessage lobbyMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("playerId", lobbyMessage.getSenderId());
        GameRoomResponse currentGameRoomResponse = this.gameRoomService.getGameRoom(lobbyMessage.getSenderId());
        return LobbyMessage.builder()
                .senderId(lobbyMessage.getSenderId())
                .type(currentGameRoomResponse.getMessageType())
                .gameRoomId(currentGameRoomResponse.getGameRoom().getId())
                .build();
    }
}
