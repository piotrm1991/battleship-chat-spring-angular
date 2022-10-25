package it.piotrmachnik.gameRoomService.repository;

import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GameRoomRepository extends MongoRepository<GameRoom, String> {
    Optional<GameRoom> findByPlayerOneId(String userId);
    Optional<GameRoom> findByPlayerTwoId(String userId);
    Optional<GameRoom> findByRoomFull(Boolean flag);
}
