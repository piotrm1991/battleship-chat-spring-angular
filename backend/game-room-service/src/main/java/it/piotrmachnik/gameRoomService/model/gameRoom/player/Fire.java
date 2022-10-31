package it.piotrmachnik.gameRoomService.model.gameRoom.player;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Fire {
    private String target;
    private boolean damage;
}
