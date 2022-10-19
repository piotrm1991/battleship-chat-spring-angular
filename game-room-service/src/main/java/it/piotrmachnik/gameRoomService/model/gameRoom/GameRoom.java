package it.piotrmachnik.gameRoomService.model.gameRoom;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Document(collection = "gameRoom")
@Builder
@Getter
@Setter
public class GameRoom {

    @Id
    private String id;

    @NotBlank
    private String chatRoomId;

    @NotBlank
    private String playerOneId;

    @NotBlank
    private String playerTwoId;

    @NotBlank
    private boolean onlinePlayerOne;

    @NotBlank
    private boolean onlinePlayerTwo;

    @NotBlank
    private boolean onPlay;

    public GameRoom(String id, String chatRoomId, String playerOneId, String playerTwoId, boolean onlinePlayerOne, boolean onlinePlayerTwo, boolean onPlay) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.onlinePlayerOne = onlinePlayerOne;
        this.onlinePlayerTwo = onlinePlayerTwo;
        this.onPlay = onPlay;
    }

    public GameRoom() {
    }
}
