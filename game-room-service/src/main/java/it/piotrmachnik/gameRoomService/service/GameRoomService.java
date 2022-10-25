package it.piotrmachnik.gameRoomService.service;

import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoom;
import it.piotrmachnik.gameRoomService.model.gameRoom.TurnStatus;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.Fire;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerGameStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerOnlineStatusType;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GameRoomService {

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public Optional<GameRoom> getByPlayerId(String userId) {
        if (gameRoomRepository.findByPlayerOneId(userId).isPresent()) {
            return gameRoomRepository.findByPlayerOneId(userId);
        } else {
            return gameRoomRepository.findByPlayerTwoId(userId);
        }
    }

    public Optional<GameRoom> getEmptyGameRoom() {
        return this.gameRoomRepository.findByRoomFull(false);
    }


    public GameRoom getGameRoomOfCurrentPlayer(String currentPlayerId) {
        if (this.gameRoomRepository.findByPlayerOneId(currentPlayerId).isPresent()) {
            return this.gameRoomRepository.findByPlayerOneId(currentPlayerId).get();
        } else if (this.gameRoomRepository.findByPlayerTwoId(currentPlayerId).isPresent()) {
            return this.gameRoomRepository.findByPlayerTwoId(currentPlayerId).get();
        } else if (this.gameRoomRepository.findByRoomFull(false).isPresent()) {
            GameRoom gameRoom = this.gameRoomRepository.findByRoomFull(false).get();
            gameRoom.setPlayerTwoId(currentPlayerId);
            gameRoom.setRoomFull(true);
            Player player = this.playerRepository.findById(currentPlayerId).get();
            player.setGameRoomId(gameRoom.getId());
            this.playerRepository.save(player);
            return this.gameRoomRepository.save(gameRoom);
        } else {
            GameRoom gameRoom = GameRoom.builder()
                    .playerOneId(currentPlayerId)
                    .roomFull(false)
                    .onPlay(false)
                    .gameOver(false)
                    .build();
            gameRoom = this.gameRoomRepository.save(gameRoom);
            Player player = this.playerRepository.findById(currentPlayerId).get();
            player.setGameRoomId(gameRoom.getId());
            this.playerRepository.save(player);
            return gameRoom;
        }
    }

    public void checkIfRoomEmptyAndHandleIt(Player disconnectedPlayer) {
        GameRoom gameRoom = this.getGameRoomById(disconnectedPlayer.getGameRoomId());
        if (gameRoom.isRoomFull()) {
            Player playerOne = this.playerRepository.findById(gameRoom.getPlayerOneId()).get();
            Player playerTwo = this.playerRepository.findById(gameRoom.getPlayerTwoId()).get();
            if (playerOne.getPlayerOnlineStatus().equals(PlayerOnlineStatusType.OFFLINE) && playerTwo.getPlayerOnlineStatus().equals(PlayerOnlineStatusType.OFFLINE)) {
                this.playerRepository.delete(playerOne);
                this.playerRepository.delete(playerTwo);
                this.gameRoomRepository.delete(gameRoom);
            }
        } else {
            this.playerRepository.delete(disconnectedPlayer);
            this.gameRoomRepository.delete(gameRoom);
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
        Player player = this.playerRepository.findById(playerId).get();
        player.setPlayerShips(ships);
        player.setPlayerGameStatus(PlayerGameStatusType.READY);
        return this.playerRepository.save(player);
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

    public boolean checkIfMissAndRecord(String currentPlayerId, String targetFire) {
        Player currentPlayer = this.playerRepository.findById(currentPlayerId).get();
        GameRoom gameRoom = this.gameRoomRepository.findById(currentPlayer.getGameRoomId()).get();
        Player enemyPlayer = this.playerRepository.findById(gameRoom.getWaitingPlayer()).get();
        AtomicBoolean fireFlag = new AtomicBoolean(false);
        enemyPlayer.getPlayerShips().forEach(ship -> {
            ship.getPlacement().forEach(placement -> {
                if (placement.getPlacement().toString().equals(targetFire)) {
                    placement.setDamage(1);
                    fireFlag.set(true);
                }
            });
        });
        this.playerRepository.save(enemyPlayer);
        currentPlayer.addFireRecord(Fire.builder()
                .target(targetFire)
                .damage(fireFlag.get())
                .build());
        this.playerRepository.save(currentPlayer);
        return fireFlag.get();
    }

    public String getShipNameIfDestroyed(GameRoom gameRoom, String targetFire) {
        Player enemyPlayer = this.playerRepository.findById(gameRoom.getWaitingPlayer()).get();
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
            this.playerRepository.save(enemyPlayer);
        }
        return shipName.get();
    }

    public boolean checkIfGameOver(String currentPlayerId) {
        Player currentPlayer = this.playerRepository.findById(currentPlayerId).get();
        GameRoom gameRoom = this.gameRoomRepository.findById(currentPlayer.getGameRoomId()).get();
        Player enemyPlayer = this.playerRepository.findById(gameRoom.getWaitingPlayer()).get();
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
