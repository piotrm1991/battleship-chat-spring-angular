package it.piotrmachnik.gameRoomService.model.gameRoom;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gameRoom")
@Builder
@Getter
@Setter
public class GameRoom {

    @Id
    private String id;
    private String playerOneId;
    private String playerTwoId;
    private boolean roomFull;
    private boolean onPlay;
    private TurnStatus currentTurn;
    private boolean pause;
    private boolean gameOver;

    public String getCurrentTurnPlayerId() {
        if (this.currentTurn.equals(TurnStatus.PLAYER_ONE)) {
            return this.playerOneId;
        } else {
            return this.playerTwoId;
        }
    }

    public String getWaitingPlayer() {
        if (this.currentTurn.equals(TurnStatus.PLAYER_ONE)) {
            return this.playerTwoId;
        } else {
            return this.playerOneId;
        }
    }

    public void goToNextTurn() {
        if (this.currentTurn.equals(TurnStatus.PLAYER_ONE)) {
            this.setCurrentTurn(TurnStatus.PLAYER_TWO);
        } else {
            this.setCurrentTurn(TurnStatus.PLAYER_ONE);
        }
    }
}
