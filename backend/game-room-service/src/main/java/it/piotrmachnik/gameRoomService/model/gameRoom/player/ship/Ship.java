package it.piotrmachnik.gameRoomService.model.gameRoom.player.ship;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Ship {
    private String name;
    private ShipStatusAndPosition position;
    private ShipStatusAndPosition status;
    private List<Placement> placement;
}
