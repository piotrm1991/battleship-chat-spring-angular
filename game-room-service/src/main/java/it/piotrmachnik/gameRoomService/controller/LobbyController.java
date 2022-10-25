package it.piotrmachnik.gameRoomService.controller;

import it.piotrmachnik.gameRoomService.model.MessageType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import it.piotrmachnik.gameRoomService.model.lobby.LobbyMessage;
import it.piotrmachnik.gameRoomService.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private PlayerService playerService;

    @MessageMapping("/lobby.newPlayer")
    public void newPlayer(@Payload final LobbyMessage lobbyMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("playerId", lobbyMessage.getSenderId());
        Player player = this.playerService.getPlayerByUserId(lobbyMessage.getSenderId());

        LobbyMessage message = LobbyMessage.builder()
                .type(MessageType.CONNECT)
                .senderId(player.getId())
                .gameRoomId(player.getGameRoomId())
                .build();
        this.simpMessagingTemplate.convertAndSend("/topic/private/" + player.getId(), message);
    }
}
