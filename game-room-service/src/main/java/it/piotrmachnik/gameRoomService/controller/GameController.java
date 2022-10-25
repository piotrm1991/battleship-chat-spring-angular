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

    @MessageMapping("/game.shootingTarget/{playerId}")
    public void shootingTarget(@Payload final GameMessage gameMessage, @DestinationVariable String playerId) {
        Player currentPlayer = this.playerService.gerPlayerById(playerId);
        GameRoom gameRoom = this.gameRoomService.getGameRoomById(currentPlayer.getGameRoomId());
        if (!gameRoom.isGameOver()) {
            gameRoom = this.gameRoomService.pauseGame(gameRoom); // check if needed
            if (gameRoom.getCurrentTurnPlayerId().equals(currentPlayer.getId())) {
                if (this.gameRoomService.checkIfMissAndRecord(currentPlayer.getId(), gameMessage.getTargetFire())) {
                    GameMessage messageCurrentPlayer = GameMessage.builder()
                            .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.AFTER_FIRE)
                            .content("boom")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageCurrentPlayer);

                    GameMessage messageEnemyPLayer = GameMessage.builder()
                            .currentPlayerId(gameRoom.getWaitingPlayer())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.ENEMY_FIRE)
                            .content("boom")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageEnemyPLayer);

                    String destroyedShip = this.gameRoomService.getShipNameIfDestroyed(gameRoom, gameMessage.getTargetFire());
                    if (!Objects.equals(destroyedShip, "")) {
                        messageCurrentPlayer = GameMessage.builder()
                                .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                                .targetFire(gameMessage.getTargetFire())
                                .type(MessageType.DESTROYED_SHIP)
                                .content("You destroyed enemy's " + destroyedShip.toUpperCase() + "!")
                                .build();
                        this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageCurrentPlayer);

                        messageEnemyPLayer = GameMessage.builder()
                                .currentPlayerId(gameRoom.getWaitingPlayer())
                                .targetFire(gameMessage.getTargetFire())
                                .type(MessageType.DESTROYED_SHIP)
                                .content("Your " + destroyedShip.toUpperCase() + " was destroyed!")
                                .build();
                        this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageEnemyPLayer);
                    }
                    // check if game over
                    if (this.gameRoomService.checkIfGameOver(currentPlayer.getId())) {
                        messageCurrentPlayer = GameMessage.builder()
                                .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                                .targetFire(gameMessage.getTargetFire())
                                .type(MessageType.GAME_OVER)
                                .content("You have won!")
                                .build();
                        this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageCurrentPlayer);

                        messageEnemyPLayer = GameMessage.builder()
                                .currentPlayerId(gameRoom.getWaitingPlayer())
                                .targetFire(gameMessage.getTargetFire())
                                .type(MessageType.GAME_OVER)
                                .content("You have lost!")
                                .build();
                        this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageEnemyPLayer);
                        return;
                    }

                } else {
                    GameMessage messageNextTurn = GameMessage.builder()
                            .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.AFTER_FIRE)
                            .content("miss")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageNextTurn);

                    GameMessage messageWaitTurn = GameMessage.builder()
                            .currentPlayerId(gameRoom.getWaitingPlayer())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.ENEMY_FIRE)
                            .content("miss")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageWaitTurn);
                }
                gameRoom = this.gameRoomService.nextTurn(gameRoom);
                gameRoom = this.gameRoomService.restartGame(gameRoom);

                GameMessage messageNextTurn = GameMessage.builder()
                        .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                        .type(MessageType.YOUR_TURN)
                        .content("Your Turn!")
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageNextTurn);

                GameMessage messageWaitTurn = GameMessage.builder()
                        .currentPlayerId(gameRoom.getWaitingPlayer())
                        .type(MessageType.ENEMY_TURN)
                        .content("Wait! Enemy's turn...")
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageWaitTurn);
            }
        }
        gameRoom = this.gameRoomService.pauseGame(gameRoom); // check if needed
        if (gameRoom.getCurrentTurnPlayerId().equals(currentPlayer.getId())) {
            if (this.gameRoomService.checkIfMissAndRecord(currentPlayer.getId(), gameMessage.getTargetFire())) {
                GameMessage messageCurrentPlayer = GameMessage.builder()
                        .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                        .targetFire(gameMessage.getTargetFire())
                        .type(MessageType.AFTER_FIRE)
                        .content("boom")
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageCurrentPlayer);

                GameMessage messageEnemyPLayer = GameMessage.builder()
                        .currentPlayerId(gameRoom.getWaitingPlayer())
                        .targetFire(gameMessage.getTargetFire())
                        .type(MessageType.ENEMY_FIRE)
                        .content("boom")
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageEnemyPLayer);

                String destroyedShip = this.gameRoomService.getShipNameIfDestroyed(gameRoom, gameMessage.getTargetFire());
                if (!Objects.equals(destroyedShip, "")) {
                    messageCurrentPlayer = GameMessage.builder()
                            .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.DESTROYED_SHIP)
                            .content("You destroyed enemy's " + destroyedShip.toUpperCase() + "!")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageCurrentPlayer);

                    messageEnemyPLayer = GameMessage.builder()
                            .currentPlayerId(gameRoom.getWaitingPlayer())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.DESTROYED_SHIP)
                            .content("Your " + destroyedShip.toUpperCase() + " was destroyed!")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageEnemyPLayer);
                }
                // check if game over
                if (this.gameRoomService.checkIfGameOver(currentPlayer.getId())) {
                    messageCurrentPlayer = GameMessage.builder()
                            .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.GAME_OVER)
                            .content("You have won!")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageCurrentPlayer);

                    messageEnemyPLayer = GameMessage.builder()
                            .currentPlayerId(gameRoom.getWaitingPlayer())
                            .targetFire(gameMessage.getTargetFire())
                            .type(MessageType.GAME_OVER)
                            .content("You have lost!")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageEnemyPLayer);
                    return;
                }

            } else {
                GameMessage messageNextTurn = GameMessage.builder()
                        .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                        .targetFire(gameMessage.getTargetFire())
                        .type(MessageType.AFTER_FIRE)
                        .content("miss")
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageNextTurn);

                GameMessage messageWaitTurn = GameMessage.builder()
                        .currentPlayerId(gameRoom.getWaitingPlayer())
                        .targetFire(gameMessage.getTargetFire())
                        .type(MessageType.ENEMY_FIRE)
                        .content("miss")
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageWaitTurn);
            }
            gameRoom = this.gameRoomService.nextTurn(gameRoom);
            gameRoom = this.gameRoomService.restartGame(gameRoom);

            GameMessage messageNextTurn = GameMessage.builder()
                    .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                    .type(MessageType.YOUR_TURN)
                    .content("Your Turn!")
                    .build();
            this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageNextTurn);

            GameMessage messageWaitTurn = GameMessage.builder()
                    .currentPlayerId(gameRoom.getWaitingPlayer())
                    .type(MessageType.ENEMY_TURN)
                    .content("Wait! Enemy's turn...")
                    .build();
            this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageWaitTurn);
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
                Player enemyPlayer = this.playerService.gerPlayerById((currentPlayer.getId().equals(gameRoom.getPlayerOneId()) ? gameRoom.getPlayerTwoId() : gameRoom.getPlayerOneId()));

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
                    GameMessage messageNextTurn = GameMessage.builder()
                            .currentPlayerId(gameRoom.getCurrentTurnPlayerId())
                            .type(MessageType.YOUR_TURN)
                            .content("Your Turn!")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getCurrentTurnPlayerId(), messageNextTurn);

                    GameMessage messageWaitTurn = GameMessage.builder()
                            .currentPlayerId(gameRoom.getWaitingPlayer())
                            .type(MessageType.ENEMY_TURN)
                            .content("Wait! Enemy's turn...")
                            .build();
                    this.simpMessagingTemplate.convertAndSend("/topic/private/game/" + gameRoom.getWaitingPlayer(), messageWaitTurn);
                }
            }
        }
    }

    @MessageMapping("/game.connectPlayer/{playerId}")
    public void newPlayerGame(@Payload final GameMessage gameMessage, @DestinationVariable String playerId, SimpMessageHeaderAccessor headerAccessor) {
        if (gameMessage.getType() == MessageType.CONNECT) {
            headerAccessor.getSessionAttributes().put("playerId", playerId);
            Player currentPlayer = this.playerService.gerPlayerById(playerId);
            GameRoom gameRoom = this.gameRoomService.getGameRoomOfCurrentPlayer(currentPlayer.getId());

            if (gameRoom.isRoomFull()) {
                Player enemyPlayer = this.playerService.gerPlayerById((currentPlayer.getId().equals(gameRoom.getPlayerOneId()) ? gameRoom.getPlayerTwoId() : gameRoom.getPlayerOneId()));

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
