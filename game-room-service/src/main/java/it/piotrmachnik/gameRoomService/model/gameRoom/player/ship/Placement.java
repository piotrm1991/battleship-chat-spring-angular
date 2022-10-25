package it.piotrmachnik.gameRoomService.model.gameRoom.player.ship;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Placement {
    Integer placement;
    Integer damage;
}
