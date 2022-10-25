package it.piotrmachnik.gameRoomService.repository;

import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player, String> {

}
