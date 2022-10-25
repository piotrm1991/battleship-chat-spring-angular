package it.piotrmachnik.gameRoomService.controller;

import it.piotrmachnik.gameRoomService.model.gameRoom.GameMessage;
import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoom;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerOnlineStatusType;
import it.piotrmachnik.gameRoomService.model.MessageType;
import it.piotrmachnik.gameRoomService.service.GameRoomService;
import it.piotrmachnik.gameRoomService.service.PlayerService;
import it.piotrmachnik.gameRoomService.service.UserService;
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

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @EventListener
    public void handleWebSocketConnectListener(final SessionConnectedEvent event) {
        LOGGER.info("New connection to Lobby!");
    }

    @EventListener
    public void handleSocketDisconnectListener(final SessionDisconnectEvent event) {
        final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        final String playerId = (String) headerAccessor.getSessionAttributes().get("playerId");
//        String player = this.userService.getPlayerName(playerId);
//        final LobbyMessage lobbyMessage = LobbyMessage.builder().type(MessageType.DISCONNECT).sender(player).senderId(playerId).build();
//        GameRoom gameRoom = this.gameRoomService.handleDisconnectFromGame(playerId);
//        sendingOperations.convertAndSend("/topic/public", lobbyMessage);
//        sendingOperations.convertAndSend("/topic/private/chat/"+gameRoomId, lobbyMessage);
        if (playerId != null) {
            Player disconnectedPlayer = this.playerService.disconnectPlayer(playerId);
            GameRoom gameRoom = this.gameRoomService.getGameRoomById(disconnectedPlayer.getGameRoomId());
            if (gameRoom.isRoomFull()) {
                Player enemyPlayer = this.playerService.gerPlayerById((disconnectedPlayer.getId().equals(gameRoom.getPlayerOneId()) ? gameRoom.getPlayerTwoId() : gameRoom.getPlayerOneId()));
                if (enemyPlayer.getPlayerOnlineStatus().equals(PlayerOnlineStatusType.ONLINE)) {
                    GameMessage message = GameMessage.builder()
                            .type(MessageType.DISCONNECT)
                            .currentPlayer(enemyPlayer.getId())
                            .enemyPlayerOnlineStatus(disconnectedPlayer.getPlayerOnlineStatus())
                            .build();
                    sendingOperations.convertAndSend("/topic/private/game/" + enemyPlayer.getId(), message);
                }
            }
            this.gameRoomService.checkIfRoomEmptyAndHandleIt(disconnectedPlayer);
        }
    }
}
