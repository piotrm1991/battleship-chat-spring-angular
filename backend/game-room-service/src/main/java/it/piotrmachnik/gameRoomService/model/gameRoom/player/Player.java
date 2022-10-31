package it.piotrmachnik.gameRoomService.model.gameRoom.player;

import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.Ship;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@Document(collection = "player")
public class Player {

    private String id;
    private String name;
    private PlayerOnlineStatusType playerOnlineStatus;
    private PlayerGameStatusType playerGameStatus;
    private List<Ship> playerShips;
    private String gameRoomId;
    private List<Fire> shootsTaken;

    public void addFireRecord(Fire fire) {
        if (this.shootsTaken == null) {
            this.shootsTaken = new ArrayList<>();
        }
        this.shootsTaken.add(fire);
    }
}
