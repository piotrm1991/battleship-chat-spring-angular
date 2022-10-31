package it.piotrmachnik.gameRoomService.controller;

import it.piotrmachnik.gameRoomService.model.MessageType;
import it.piotrmachnik.gameRoomService.model.gameRoom.GameMessage;
import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoom;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerGameStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerOnlineStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.Ship;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.ShipMessage;
import it.piotrmachnik.gameRoomService.service.GameRoomService;
import it.piotrmachnik.gameRoomService.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Controller
public class GameController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private PlayerService playerService;

    private void sendMessages(GameRoom gameRoom, MessageType messageType, String targetFire, String destroyedShipName) {
        Player currentPlayer = this.playerService.getPlayerById(gameRoom.getCurrentTurnPlayerId());
        Player enemyPlayer = this.playerService.getPlayerById(gameRoom.getWaitingPlayer());
        GameMessage messageCurrentPlayer = GameMessage.builder()
                .currentPlayerId(currentPlayer.getId())
                .build();
        GameMessage messageEnemyPlayer = GameMessage.builder()
                .currentPlayerId(enemyPlayer.getId())
                .build();
        switch (messageType) {
            case AFTER_FIRE_HIT:
                messageCurrentPlayer.setType(MessageType.AFTER_FIRE);
                messageCurrentPlayer.setTargetFire(targetFire);
                messageCurrentPlayer.setContent("boom");

                messageEnemyPlayer.setType(MessageType.ENEMY_FIRE);
                messageEnemyPlayer.setTargetFire(targetFire);
                messageEnemyPlayer.setContent("boom");
                break;
            case DESTROYED_SHIP:
                messageCurrentPlayer.setTargetFire(targetFire);
                messageCurrentPlayer.setType(MessageType.DESTROYED_SHIP);
                messageCurrentPlayer.setContent("You destroyed enemy's " + destroyedShipName.toUpperCase() + "!");

                messageEnemyPlayer.setType(MessageType.DESTROYED_SHIP);
                messageEnemyPlayer.setTargetFire(targetFire);
                messageEnemyPlayer.setContent("Your " + destroyedShipName.toUpperCase() + " was destroyed!");
                break;
            case GAME_OVER:
                messageCurrentPlayer.setType(MessageType.GAME_OVER);
                messageCurrentPlayer.setContent("You have WON!");

                messageEnemyPlayer.setType(MessageType.GAME_OVER);
                messageEnemyPlayer.setContent("You have LOST!");
                break;
            case AFTER_FIRE_MISS:
                messageCurrentPlayer.setType(MessageType.AFTER_FIRE);
                messageCurrentPlayer.setTargetFire(targetFire);
                messageCurrentPlayer.setContent("miss");

                messageEnemyPlayer.setType(MessageType.ENEMY_FIRE);
                messageEnemyPlayer.setTargetFire(targetFire);
                messageEnemyPlayer.setContent("miss");
                break;
            case NEXT_TURN:
                messageCurrentPlayer.setType(MessageType.YOUR_TURN);
                messageCurrentPlayer.setContent("Your Turn!");

                messageEnemyPlayer.setType(MessageType.ENEMY_TURN);
                messageEnemyPlayer.setContent("Wait! Enemy's turn...");
                break;
        }

        this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageCurrentPlayer);
        this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageEnemyPlayer);
    }

    @MessageMapping("/game.shootingTarget/{playerId}")
    public void shootingTarget(@Payload final GameMessage gameMessage, @DestinationVariable String playerId) {
        Player currentPlayer = this.playerService.getPlayerById(playerId);
        GameRoom gameRoom = this.gameRoomService.getGameRoomById(currentPlayer.getGameRoomId());
        if (!gameRoom.isGameOver() && !gameRoom.isPause()) {
            gameRoom = this.gameRoomService.pauseGame(gameRoom); // check if needed
            if (gameRoom.getCurrentTurnPlayerId().equals(currentPlayer.getId())) {
                if (this.gameRoomService.checkIfMissOrHitAndRecord(currentPlayer.getId(), gameMessage.getTargetFire())) {

                    this.sendMessages(gameRoom, MessageType.AFTER_FIRE_HIT, gameMessage.getTargetFire(), "");

                    String destroyedShip = this.gameRoomService.setShipFlagAndGetNameIfDestroyed(gameRoom, gameMessage.getTargetFire());
                    if (!Objects.equals(destroyedShip, "")) {

                        this.sendMessages(gameRoom, MessageType.DESTROYED_SHIP, gameMessage.getTargetFire(), destroyedShip);
                    }
                    // check if game over
                    if (this.gameRoomService.checkIfGameOver(currentPlayer.getId())) {

                        this.sendMessages(gameRoom, MessageType.GAME_OVER, "", "");
                        return;
                    }

                } else {

                    this.sendMessages(gameRoom, MessageType.AFTER_FIRE_MISS, gameMessage.getTargetFire(), "");
                }
                gameRoom = this.gameRoomService.nextTurn(gameRoom);
                gameRoom = this.gameRoomService.restartGame(gameRoom);

                this.sendMessages(gameRoom, MessageType.NEXT_TURN, "", "");
            }
        }
    }

    @MessageMapping("/game.setShips/{playerId}")
    public void setPlayerShips(@Payload final GameMessage gameMessage, @DestinationVariable String playerId) {

        // later check if player did not temper with ship size
        if (gameMessage.getShipsPlacement().size() != 5) {
            // NOT_READY message
            GameMessage message = GameMessage.builder()
                    .type(MessageType.NOT_READY)
                    .currentPlayerId(playerId)
                    .content("Please, place all your ships before playing.")
                    .build();
            this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + playerId, message);
            return;
        }
        if (gameMessage.getShipsPlacement().size() == 5) {
            Player currentPlayer = this.gameRoomService.setShips(gameMessage.getShipsPlacement(), playerId);
            GameRoom gameRoom = this.gameRoomService.getGameRoomById(currentPlayer.getGameRoomId());
            GameMessage message = GameMessage.builder()
                    .currentPlayerId(currentPlayer.getId())
                    .type(MessageType.READY)
                    .currentPlayerGameStatus(currentPlayer.getPlayerGameStatus())
                    .build();
            this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + playerId, message);
            if (gameRoom.isRoomFull()) {
                Player enemyPlayer = this.playerService.getPlayerById((currentPlayer.getId().equals(gameRoom.getPlayerOneId()) ? gameRoom.getPlayerTwoId() : gameRoom.getPlayerOneId()));

                if (enemyPlayer != null) {
                    GameMessage enemyMessage = GameMessage.builder()
                            .currentPlayerId(enemyPlayer.getId())
                            .type(MessageType.READY)
                            .enemyPlayerGameStatus(currentPlayer.getPlayerGameStatus())
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + enemyPlayer.getId(), enemyMessage);
                }

                // check if both player are ready
                if (currentPlayer.getPlayerGameStatus().equals(PlayerGameStatusType.READY) && enemyPlayer.getPlayerGameStatus().equals(PlayerGameStatusType.READY)) {
                    gameRoom = this.gameRoomService.startGameNextTurn(gameRoom);

                    this.sendMessages(gameRoom, MessageType.NEXT_TURN, "", "");
                }
            }
        }
    }

    @MessageMapping("/game.connectPlayer/{playerId}")
    public void newPlayerGame(@Payload final GameMessage gameMessage, @DestinationVariable String playerId, SimpMessageHeaderAccessor headerAccessor) {
        if (gameMessage.getType() == MessageType.CONNECT) {
            headerAccessor.getSessionAttributes().put("playerId", playerId);
            Player currentPlayer = this.playerService.getPlayerById(playerId);
            GameRoom gameRoom = this.gameRoomService.getGameRoomOfCurrentPlayer(currentPlayer.getId());

            if (gameRoom.isRoomFull()) {
                Player enemyPlayer = this.playerService.getPlayerById((currentPlayer.getId().equals(gameRoom.getPlayerOneId()) ? gameRoom.getPlayerTwoId() : gameRoom.getPlayerOneId()));

                GameMessage currentPlayerMessage = GameMessage.builder()
                        .currentPlayerId(currentPlayer.getId())
                        .currentPlayer(currentPlayer.getName())
                        .enemyPlayerId(enemyPlayer.getId())
                        .enemyPlayer(enemyPlayer.getName())
                        .currentPlayerOnlineStatus(currentPlayer.getPlayerOnlineStatus())
                        .currentPlayerGameStatus(currentPlayer.getPlayerGameStatus())
                        .enemyPlayerOnlineStatus(enemyPlayer.getPlayerOnlineStatus())
                        .enemyPlayerGameStatus(enemyPlayer.getPlayerGameStatus())
                        .shipsPlacement((currentPlayer.getPlayerShips() != null) ? this.parseShipsForMessage(currentPlayer.getPlayerShips()) : null)
                        .currentPlayerShoots(currentPlayer.getShootsTaken())
                        .enemyPlayerShoots(enemyPlayer.getShootsTaken())
                        .type(MessageType.CONNECT)
                        .content("Player " + gameMessage.getCurrentPlayer() + " connected to GameRoom " + gameMessage.getGameRoomId())
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + playerId, currentPlayerMessage);

                GameMessage enemyPlayerMessage = GameMessage.builder()
                    .currentPlayerId(enemyPlayer.getId())
                    .currentPlayer(enemyPlayer.getName())
                    .enemyPlayerId(currentPlayer.getId())
                    .enemyPlayer(currentPlayer.getName())
                    .enemyPlayerOnlineStatus(currentPlayer.getPlayerOnlineStatus())
                    .enemyPlayerGameStatus(currentPlayer.getPlayerGameStatus())
                    .currentPlayerOnlineStatus(enemyPlayer.getPlayerOnlineStatus())
                    .currentPlayerGameStatus(enemyPlayer.getPlayerGameStatus())
                    .type(MessageType.CONNECT_ENEMY)
                    .content("Player " + gameMessage.getCurrentPlayer() + " connected to GameRoom " + gameMessage.getGameRoomId())
                    .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + enemyPlayer.getId(), enemyPlayerMessage);

                if (gameRoom.isOnPlay() && gameRoom.getCurrentTurnPlayerId().equals(currentPlayer.getId())) {
                    GameMessage messageYourTurn = GameMessage.builder()
                            .currentPlayerId(currentPlayer.getId())
                            .type(MessageType.YOUR_TURN)
                            .content("Your Turn!")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + currentPlayer.getId(), messageYourTurn);
                } else {
                    GameMessage messageYourTurn = GameMessage.builder()
                            .currentPlayerId(currentPlayer.getId())
                            .type(MessageType.ENEMY_TURN)
                            .content("Wait! Enemy's turn...")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + currentPlayer.getId(), messageYourTurn);
                }
            } else {
                GameMessage currentPlayerMessage = GameMessage.builder()
                        .currentPlayerId(currentPlayer.getId())
                        .currentPlayer(currentPlayer.getName())
                        .enemyPlayerId(null)
                        .enemyPlayer(null)
                        .currentPlayerOnlineStatus(currentPlayer.getPlayerOnlineStatus())
                        .currentPlayerGameStatus(currentPlayer.getPlayerGameStatus())
                        .enemyPlayerOnlineStatus(PlayerOnlineStatusType.OFFLINE)
                        .enemyPlayerGameStatus(PlayerGameStatusType.NOT_READY)
                        .shipsPlacement((currentPlayer.getPlayerShips() != null) ? this.parseShipsForMessage(currentPlayer.getPlayerShips()) : null)
                        .type(MessageType.CONNECT)
                        .content("Player " + gameMessage.getCurrentPlayer() + " connected to GameRoom " + gameMessage.getGameRoomId())
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + playerId, currentPlayerMessage);
            }
        }
    }

    private List<ShipMessage> parseShipsForMessage(List<Ship> playerShips) {
        List<ShipMessage> shipMessage = new ArrayList<>();
        playerShips.forEach(ship -> {
            List<Integer> placement = new ArrayList<>();
            ship.getPlacement().forEach(p -> {
                placement.add(p.getPlacement());
            });
            shipMessage.add(ShipMessage.builder()
                    .name(ship.getName())
                    .position(ship.getPosition().name().toLowerCase(Locale.ROOT))
                    .placement(placement)
                    .build());
        });
        return shipMessage;
    }
}
