package it.piotrmachnik.gameRoomService.model.gameRoom;

import it.piotrmachnik.gameRoomService.model.MessageType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.Fire;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerGameStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerOnlineStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.ShipMessage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GameMessage {
    private MessageType type;
    private String content;
    private String currentPlayerId;
    private String currentPlayer;
    private String enemyPlayerId;
    private String enemyPlayer;
    private PlayerOnlineStatusType enemyPlayerOnlineStatus;
    private PlayerOnlineStatusType currentPlayerOnlineStatus;
    private PlayerGameStatusType enemyPlayerGameStatus;
    private PlayerGameStatusType currentPlayerGameStatus;
    private String gameRoomId;
    private List<ShipMessage> shipsPlacement;
    private String targetFire;
    private List<Fire> currentPlayerShoots;
    private List<Fire> enemyPlayerShoots;
}
