package it.piotrmachnik.gameRoomService.service;

import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoom;
import it.piotrmachnik.gameRoomService.model.gameRoom.TurnStatus;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.*;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.Placement;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.Ship;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.ShipMessage;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.ShipStatusAndPosition;
import it.piotrmachnik.gameRoomService.repository.GameRoomRepository;
import it.piotrmachnik.gameRoomService.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GameRoomService {

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerService playerService;

    public GameRoomService(final GameRoomRepository gameRoomRepository, final PlayerRepository playerRepository, final PlayerService playerService) {
        this.gameRoomRepository = gameRoomRepository;
        this.playerRepository = playerRepository;
        this.playerService = playerService;
    }

    public GameRoomService() {
    }

//    public Optional<GameRoom> getByPlayerId(String userId) {
//        if (gameRoomRepository.findByPlayerOneId(userId).isPresent()) {
//            return gameRoomRepository.findByPlayerOneId(userId);
//        } else {
//            return gameRoomRepository.findByPlayerTwoId(userId);
//        }
//    }
//
//    public Optional<GameRoom> getEmptyGameRoom() {
//        return this.gameRoomRepository.findByRoomFull(false);
//    }


    public GameRoom getGameRoomOfCurrentPlayer(String currentPlayerId) {
        if (this.gameRoomRepository.findByPlayerOneId(currentPlayerId).isPresent()) {
            return this.gameRoomRepository.findByPlayerOneId(currentPlayerId).get();
        } else if (this.gameRoomRepository.findByPlayerTwoId(currentPlayerId).isPresent()) {
            return this.gameRoomRepository.findByPlayerTwoId(currentPlayerId).get();
        } else if (this.gameRoomRepository.findByRoomFull(false).isPresent()) {
            GameRoom gameRoom = this.gameRoomRepository.findByRoomFull(false).get();
            gameRoom.setPlayerTwoId(currentPlayerId);
            gameRoom.setRoomFull(true);
            Player player = this.playerService.setGameRoomIdForPlayer(currentPlayerId, gameRoom.getId());
            this.playerService.savePlayer(player);
            return this.gameRoomRepository.save(gameRoom);
        } else {
            GameRoom gameRoom = GameRoom.builder()
                    .playerOneId(currentPlayerId)
                    .roomFull(false)
                    .onPlay(false)
                    .gameOver(false)
                    .build();
            gameRoom = this.gameRoomRepository.save(gameRoom);
            Player player = this.playerService.setGameRoomIdForPlayer(currentPlayerId, gameRoom.getId());
            this.playerService.savePlayer(player);
            return gameRoom;
        }
    }

    public void checkIfRoomEmptyAndHandleIt(Player disconnectedPlayer) {
        if (this.gameRoomRepository.findById(disconnectedPlayer.getGameRoomId()).isPresent()) {
            GameRoom gameRoom = this.gameRoomRepository.findById(disconnectedPlayer.getGameRoomId()).get();
            if (gameRoom.isRoomFull()) {
                Player playerOne = this.playerService.getPlayerById(gameRoom.getPlayerOneId());
                Player playerTwo = this.playerService.getPlayerById(gameRoom.getPlayerTwoId());
                if (playerOne.getPlayerOnlineStatus().equals(PlayerOnlineStatusType.OFFLINE) && playerTwo.getPlayerOnlineStatus().equals(PlayerOnlineStatusType.OFFLINE)) {
                    this.playerService.deletePlayer(playerOne);
                    this.playerService.deletePlayer(playerTwo);
                    this.gameRoomRepository.delete(gameRoom);
                }
            } else {
                this.playerService.deletePlayer(disconnectedPlayer);
                this.gameRoomRepository.delete(gameRoom);
            }
        }
    }

    public GameRoom getGameRoomById(String gameRoomId) {
        return this.gameRoomRepository.findById(gameRoomId).get();
    }

    public Player setShips(List<ShipMessage> shipsPlacement, String playerId) {
        List<Ship> ships = new ArrayList<>();
        shipsPlacement.forEach(s -> {
            List<Placement> placements = new ArrayList<>();
            s.getPlacement().forEach(p -> {
                placements.add(Placement.builder()
                        .placement(p)
                        .damage(0)
                        .build());
            });
            Ship ship = Ship.builder()
                    .name(s.getName())
                    .placement(placements)
                    .status(ShipStatusAndPosition.FUNCTIONAL)
                    .position((placements.get(0).getPlacement() - placements.get(1).getPlacement() == -1) ? ShipStatusAndPosition.HORIZONTAL : ShipStatusAndPosition.VERTICAL)
                    .build();
            ships.add(ship);
        });
        Player player = this.playerService.getPlayerById(playerId);
        player.setPlayerShips(ships);
        player.setPlayerGameStatus(PlayerGameStatusType.READY);
        return this.playerService.savePlayer(player);
    }

    public GameRoom startGameNextTurn(GameRoom gameRoom) {
        gameRoom.setOnPlay(true);
        gameRoom.setCurrentTurn(TurnStatus.PLAYER_ONE);
        return this.gameRoomRepository.save(gameRoom);
    }

    public GameRoom nextTurn(GameRoom gameRoom) {
        gameRoom.goToNextTurn();
        return this.gameRoomRepository.save(gameRoom);
    }

    public GameRoom pauseGame(GameRoom gameRoom) {
        gameRoom.setPause(true);
        return this.gameRoomRepository.save(gameRoom);
    }

    public GameRoom restartGame(GameRoom gameRoom) {
        gameRoom.setPause(false);
        return this.gameRoomRepository.save(gameRoom);
    }

    public boolean checkIfMissOrHitAndRecord(String currentPlayerId, String targetFire) {
        Player currentPlayer = this.playerService.getPlayerById(currentPlayerId);
        GameRoom gameRoom = this.gameRoomRepository.findById(currentPlayer.getGameRoomId()).get();
        Player enemyPlayer = this.playerService.getPlayerById(gameRoom.getWaitingPlayer());
        AtomicBoolean fireFlag = new AtomicBoolean(false);
        enemyPlayer.getPlayerShips().forEach(ship -> {
            ship.getPlacement().forEach(placement -> {
                if (placement.getPlacement().toString().equals(targetFire)) {
                    placement.setDamage(1);
                    fireFlag.set(true);
                }
            });
        });
        if (fireFlag.get()) {
            this.playerService.savePlayer(enemyPlayer);
        }
        currentPlayer.addFireRecord(Fire.builder()
                .target(targetFire)
                .damage(fireFlag.get())
                .build());
        this.playerService.savePlayer(currentPlayer);
        return fireFlag.get();
    }

    public String setShipFlagAndGetNameIfDestroyed(GameRoom gameRoom, String targetFire) {
        Player enemyPlayer = this.playerService.getPlayerById(gameRoom.getWaitingPlayer());
        AtomicReference<String> shipName = new AtomicReference<>("");
        enemyPlayer.getPlayerShips().forEach(ship -> {
            AtomicBoolean flag = new AtomicBoolean(false);
            ship.getPlacement().forEach(placement -> {
                if (placement.getPlacement().toString().equals(targetFire)) {
                    flag.set(true);
                }
            });
            if (flag.get()) {
                ship.getPlacement().forEach(placement -> {
                    if (placement.getDamage() == 0) {
                        flag.set(false);
                    }
                });
            }
            if (flag.get()) {
                ship.setStatus(ShipStatusAndPosition.DESTROYED);
                shipName.set(ship.getName());
            }
        });
        if (!shipName.get().equals("")) {
            this.playerService.savePlayer(enemyPlayer);
        }
        return shipName.get();
    }

    public boolean checkIfGameOver(String currentPlayerId) {
        Player currentPlayer = this.playerService.getPlayerById(currentPlayerId);
        GameRoom gameRoom = this.gameRoomRepository.findById(currentPlayer.getGameRoomId()).get();
        Player enemyPlayer = this.playerService.getPlayerById(gameRoom.getWaitingPlayer());
        AtomicBoolean flag = new AtomicBoolean(true);
        enemyPlayer.getPlayerShips().forEach(ship -> {
            if (ship.getStatus().equals(ShipStatusAndPosition.FUNCTIONAL)) {
                flag.set(false);
            }
        });
        if (flag.get()) {
            gameRoom.setGameOver(true);
            this.gameRoomRepository.save(gameRoom);
        }
        return flag.get();
    }
}
