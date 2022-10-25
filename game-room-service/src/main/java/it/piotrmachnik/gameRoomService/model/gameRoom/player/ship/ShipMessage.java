package it.piotrmachnik.gameRoomService.model.gameRoom.player.ship;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ShipMessage {
    private String name;
    private String position;
    private List<Integer> placement;
}
