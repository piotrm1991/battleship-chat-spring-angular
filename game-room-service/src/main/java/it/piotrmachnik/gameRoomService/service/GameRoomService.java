package it.piotrmachnik.gameRoomService.service;

import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoom;
import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoomResponse;
import it.piotrmachnik.gameRoomService.model.MessageType;
import it.piotrmachnik.gameRoomService.repository.GameRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class GameRoomService {
    @Autowired
    private GameRoomRepository gameRoomRepository;

    public Optional<GameRoom> getByPlayerId(String userId) {
        if (gameRoomRepository.findByPlayerOneId(userId).isPresent()) {
            return gameRoomRepository.findByPlayerOneId(userId);
        } else {
            return gameRoomRepository.findByPlayerTwoId(userId);
        }
    }

    public Optional<GameRoom> getEmptyGameRoom() {
        return this.gameRoomRepository.findByOnPlay(false);
    }

    public GameRoomResponse getGameRoom(String userId) {

        if (gameRoomRepository.findByPlayerOneId(userId).isPresent()) {

            GameRoom gameRoom = gameRoomRepository.findByPlayerOneId(userId).get();
            gameRoom.setOnlinePlayerOne(true);
            return GameRoomResponse.builder()
                    .gameRoom(this.gameRoomRepository.save(gameRoom))
                    .messageType(MessageType.GAME_STARTED)
                    .build();
        } else if (gameRoomRepository.findByPlayerTwoId(userId).isPresent()) {

            GameRoom gameRoom = gameRoomRepository.findByPlayerTwoId(userId).get();
            gameRoom.setOnlinePlayerTwo(true);
            return GameRoomResponse.builder()
                    .gameRoom(this.gameRoomRepository.save(gameRoom))
                    .messageType(MessageType.GAME_STARTED)
                    .build();
        } else if (getEmptyGameRoom().isPresent()) {

            GameRoom gameRoom = getEmptyGameRoom().get();
            gameRoom.setPlayerTwoId(userId);
            gameRoom.setOnlinePlayerTwo(true);
            gameRoom.setOnPlay(true);
            return GameRoomResponse.builder()
                    .gameRoom(this.gameRoomRepository.save(gameRoom))
                    .messageType(MessageType.GAME_STARTED)
                    .build();
        } else {

            return GameRoomResponse.builder()
                    .gameRoom(this.gameRoomRepository.save(GameRoom.builder().playerOneId(userId).onlinePlayerOne(true).build()))
                    .messageType(MessageType.NEW_GAME_ROOM)
                    .build();
        }
    }

    public String handleDisconnectFromGame(String playerId) {
        String gameRoomId = "";
        if (getByPlayerId(playerId).isPresent()) {
            GameRoom gameRoom = getByPlayerId(playerId).get();
            if (Objects.equals(gameRoom.getPlayerOneId(), playerId)) {
                gameRoom.setOnlinePlayerOne(false);
            } else {
                gameRoom.setOnlinePlayerTwo(false);
            }
            gameRoomId = this.gameRoomRepository.save(gameRoom).getId();
            if (!gameRoom.isOnlinePlayerOne() && !gameRoom.isOnlinePlayerTwo()) {
                this.gameRoomRepository.delete(gameRoom);
            }
        }
        return gameRoomId;
    }
}
