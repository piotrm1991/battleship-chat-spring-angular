package it.piotrmachnik.gameRoomService.service;

import it.piotrmachnik.gameRoomService.model.gameRoom.GameRoom;
import it.piotrmachnik.gameRoomService.model.gameRoom.TurnStatus;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerGameStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerOnlineStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.Placement;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.Ship;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.ShipMessage;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.ship.ShipStatusAndPosition;
import it.piotrmachnik.gameRoomService.repository.GameRoomRepository;
import it.piotrmachnik.gameRoomService.repository.PlayerRepository;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GameRoomServiceMockTest extends TestCase {

    private GameRoomService gameRoomService;

    @Mock
    private GameRoomRepository gameRoomRepositoryMock;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerService playerServiceMock;

    @Before
    public void setup() {
        this.gameRoomService = new GameRoomService(gameRoomRepositoryMock, playerRepository, playerServiceMock);
    }

    @Test
    public void givenPlayerIdWhenGetGameRoomOfPlayerThenGameRoomPlayerOne() {
        // Given
        String playerId = "1";

        Optional<GameRoom> resultFindGameRoomPlayerOne = Optional.of(
                GameRoom.builder()
                        .id("11")
                        .playerOneId("1")
                        .playerTwoId("2")
                        .build());

        Mockito.when(this.gameRoomRepositoryMock.findByPlayerOneId(playerId)).thenReturn(resultFindGameRoomPlayerOne);
        // When
        GameRoom result = this.gameRoomService.getGameRoomOfCurrentPlayer(playerId);
        GameRoom expected = GameRoom.builder()
                                        .id("11")
                                        .playerOneId("1")
                                        .playerTwoId("2")
                                        .build();
        // Then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
        verify(this.gameRoomRepositoryMock, times(2)).findByPlayerOneId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(0)).findByPlayerTwoId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(0)).findByRoomFull(Mockito.any(Boolean.class));
    }

    @Test
    public void givenPlayerIdWhenGetGameRoomOfPlayerThenGameRoomPlayerTwo() {
        // Given
        String playerId = "2";

        Optional<GameRoom> resultFindGameRoomPlayerTwo = Optional.of(
                GameRoom.builder()
                        .id("11")
                        .playerOneId("1")
                        .playerTwoId("2")
                        .build());

        Mockito.when(this.gameRoomRepositoryMock.findByPlayerOneId(playerId)).thenReturn(Optional.empty());
        Mockito.when(this.gameRoomRepositoryMock.findByPlayerTwoId(playerId)).thenReturn(resultFindGameRoomPlayerTwo);
        // When
        GameRoom result = this.gameRoomService.getGameRoomOfCurrentPlayer(playerId);
        GameRoom expected = GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .playerTwoId("2")
                .build();
        // Then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
        verify(this.gameRoomRepositoryMock, times(1)).findByPlayerOneId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(2)).findByPlayerTwoId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(0)).findByRoomFull(Mockito.any(Boolean.class));
    }

    @Test
    public void givenPlayerIdWhenGetGameRoomOfPlayerThenGameRoomNotFull() {
        // Given
        String playerId = "2";

        Optional<GameRoom> resultFindGameRoomNotFull = Optional.of(
                GameRoom.builder()
                        .id("11")
                        .playerOneId("1")
                        .roomFull(false)
                        .build());

        Player player = Player.builder()
                .id("1")
                .name("testPlayer")
                .gameRoomId("11")
                .build();

        GameRoom expected = GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .playerTwoId("2")
                .roomFull(true)
                .build();

        Mockito.when(this.gameRoomRepositoryMock.findByPlayerOneId(playerId)).thenReturn(Optional.empty());
        Mockito.when(this.gameRoomRepositoryMock.findByPlayerTwoId(playerId)).thenReturn(Optional.empty());
        Mockito.when(this.gameRoomRepositoryMock.findByRoomFull(false)).thenReturn(resultFindGameRoomNotFull);
        Mockito.when(this.playerServiceMock.setGameRoomIdForPlayer(playerId, "11")).thenReturn(player);
        Mockito.when(this.gameRoomRepositoryMock.save(Mockito.any(GameRoom.class))).thenReturn(expected);
        // When
        GameRoom result = this.gameRoomService.getGameRoomOfCurrentPlayer(playerId);
        // Then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
        verify(this.gameRoomRepositoryMock, times(1)).findByPlayerOneId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).findByPlayerTwoId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(2)).findByRoomFull(Mockito.any(Boolean.class));
        verify(this.playerServiceMock, times(1)).setGameRoomIdForPlayer(Mockito.any(String.class), Mockito.any(String.class));
        verify(this.playerServiceMock, times(1)).savePlayer(Mockito.any(Player.class));
        verify(this.gameRoomRepositoryMock, times(1)).save(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenPlayerIdWhenGetGameRoomOfPlayerThenGameRoomNew() {
        // Given
        String playerId = "1";

        Player player = Player.builder()
                .id("1")
                .name("testPlayer")
                .gameRoomId("11")
                .build();

        GameRoom expected = GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .roomFull(false)
                .gameOver(false)
                .onPlay(false)
                .build();

        Mockito.when(this.gameRoomRepositoryMock.findByPlayerOneId(playerId)).thenReturn(Optional.empty());
        Mockito.when(this.gameRoomRepositoryMock.findByPlayerTwoId(playerId)).thenReturn(Optional.empty());
        Mockito.when(this.gameRoomRepositoryMock.findByRoomFull(false)).thenReturn(Optional.empty());
        Mockito.when(this.playerServiceMock.setGameRoomIdForPlayer(playerId, "11")).thenReturn(player);
        Mockito.when(this.gameRoomRepositoryMock.save(Mockito.any(GameRoom.class))).thenReturn(expected);
        // When
        GameRoom result = this.gameRoomService.getGameRoomOfCurrentPlayer(playerId);
        // Then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
        verify(this.gameRoomRepositoryMock, times(1)).findByPlayerOneId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).findByPlayerTwoId(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).findByRoomFull(Mockito.any(Boolean.class));
        verify(this.playerServiceMock, times(1)).setGameRoomIdForPlayer(Mockito.any(String.class), Mockito.any(String.class));
        verify(this.playerServiceMock, times(1)).savePlayer(Mockito.any(Player.class));
        verify(this.gameRoomRepositoryMock, times(1)).save(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenDisconnectedPlayerWhenWhenRoomFullAndCheckIfRoomEmptyHandleItThenHandleIt() {
        // Given
        Player disconnectedPlayer = Player.builder()
                .id("1")
                .gameRoomId("11")
                .playerOnlineStatus(PlayerOnlineStatusType.OFFLINE)
                .build();

        Player playerTwo = Player.builder()
                .id("2")
                .gameRoomId("11")
                .playerOnlineStatus(PlayerOnlineStatusType.OFFLINE)
                .build();

        Optional<GameRoom> gameRoom = Optional.of(GameRoom.builder()
                    .id("11")
                    .playerOneId("1")
                    .playerTwoId("2")
                    .roomFull(true)
                    .build());

        Mockito.when(this.gameRoomRepositoryMock.findById("11")).thenReturn(gameRoom);
        Mockito.when(this.playerServiceMock.getPlayerById("1")).thenReturn(disconnectedPlayer);
        Mockito.when(this.playerServiceMock.getPlayerById("2")).thenReturn(playerTwo);
        // When
        this.gameRoomService.checkIfRoomEmptyAndHandleIt(disconnectedPlayer);
        // Then
        verify(this.gameRoomRepositoryMock, times(2)).findById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(2)).getPlayerById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(2)).deletePlayer(Mockito.any(Player.class));
        verify(this.gameRoomRepositoryMock, times(1)).delete(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenDisconnectedPlayerWhenWhenRoomNotFullAndCheckIfRoomEmptyHandleItThenHandleIt() {
        // Given
        Player disconnectedPlayer = Player.builder()
                .id("1")
                .gameRoomId("11")
                .playerOnlineStatus(PlayerOnlineStatusType.OFFLINE)
                .build();

        Optional<GameRoom> gameRoom = Optional.of(GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .roomFull(false)
                .build());

        Mockito.when(this.gameRoomRepositoryMock.findById("11")).thenReturn(gameRoom);
        // When
        this.gameRoomService.checkIfRoomEmptyAndHandleIt(disconnectedPlayer);
        // Then
        verify(this.gameRoomRepositoryMock, times(2)).findById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(0)).getPlayerById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(1)).deletePlayer(Mockito.any(Player.class));
        verify(this.gameRoomRepositoryMock, times(1)).delete(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenShipPlacementMessageAndPlayerIdWhenSetShipsThenReturnPlayerWithShips() {
        // Given
        String playerId = "1";
        List<ShipMessage> shipsPlacement = Arrays.asList(
                ShipMessage.builder()
                        .name("destroyer")
                        .placement(Arrays.asList(1,2,3))
                        .build(),
                ShipMessage.builder()
                        .name("cruiser")
                        .placement(Arrays.asList(5,10,15,20))
                        .build()
        );
        Player player = Player.builder()
                .id("1")
                .name("testPlayer")
                .playerGameStatus(PlayerGameStatusType.NOT_READY)
                .build();
        Player expectedPlayer = Player.builder()
                .id("1")
                .name("testPlayer")
                .playerGameStatus(PlayerGameStatusType.READY)
                .playerShips(Arrays.asList(
                        Ship.builder()
                                .name("destroyer")
                                .position(ShipStatusAndPosition.VERTICAL)
                                .status(ShipStatusAndPosition.FUNCTIONAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .damage(0)
                                                .placement(1)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(2)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(3)
                                                .build()
                                ))
                                .build(),
                        Ship.builder()
                                .name("cruiser")
                                .position(ShipStatusAndPosition.VERTICAL)
                                .status(ShipStatusAndPosition.HORIZONTAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .damage(0)
                                                .placement(5)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(10)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(15)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(20)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        Mockito.when(this.playerServiceMock.getPlayerById("1")).thenReturn(player);
        Mockito.when(this.playerServiceMock.savePlayer(Mockito.any(Player.class))).thenReturn(expectedPlayer);
        // When
        Player resultPlayer = this.gameRoomService.setShips(shipsPlacement, playerId);
        // Then
        Assert.assertTrue(new ReflectionEquals(expectedPlayer).matches(resultPlayer));
        verify(this.playerServiceMock, times(1)).getPlayerById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(1)).savePlayer(Mockito.any(Player.class));
    }

    @Test
    public void givenGameRoomWhenStartGameNextTurnThenReturnGameRoom() {
        GameRoom gameRoom = GameRoom.builder()
                .id("1")
                .onPlay(false)
                .build();
        GameRoom expectedGameRoom = GameRoom.builder()
                .id("1")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .build();
        Mockito.when(this.gameRoomRepositoryMock.save(Mockito.any(GameRoom.class))).thenReturn(expectedGameRoom);

        GameRoom result = this.gameRoomService.startGameNextTurn(gameRoom);

        Assert.assertTrue(new ReflectionEquals(expectedGameRoom).matches(result));
        verify(this.gameRoomRepositoryMock, times(1)).save(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenGameRoomWhenNextTurnThenReturnGameRoom() {
        GameRoom gameRoom = GameRoom.builder()
                .id("1")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .build();
        GameRoom expectedGameRoom = GameRoom.builder()
                .id("1")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_TWO)
                .build();
        Mockito.when(this.gameRoomRepositoryMock.save(Mockito.any(GameRoom.class))).thenReturn(expectedGameRoom);

        GameRoom result = this.gameRoomService.startGameNextTurn(gameRoom);

        Assert.assertTrue(new ReflectionEquals(expectedGameRoom).matches(result));
        verify(this.gameRoomRepositoryMock, times(1)).save(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenGameRoomWhenPauseGameThenReturnGameRoom() {
        GameRoom gameRoom = GameRoom.builder()
                .id("1")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .pause(false)
                .build();
        GameRoom expectedGameRoom = GameRoom.builder()
                .id("1")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .pause(true)
                .build();
        Mockito.when(this.gameRoomRepositoryMock.save(Mockito.any(GameRoom.class))).thenReturn(expectedGameRoom);

        GameRoom result = this.gameRoomService.startGameNextTurn(gameRoom);

        Assert.assertTrue(new ReflectionEquals(expectedGameRoom).matches(result));
        verify(this.gameRoomRepositoryMock, times(1)).save(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenGameRoomWhenRestartGameThenReturnGameRoom() {
        GameRoom gameRoom = GameRoom.builder()
                .id("1")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .pause(true)
                .build();
        GameRoom expectedGameRoom = GameRoom.builder()
                .id("1")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .pause(false)
                .build();
        Mockito.when(this.gameRoomRepositoryMock.save(Mockito.any(GameRoom.class))).thenReturn(expectedGameRoom);

        GameRoom result = this.gameRoomService.startGameNextTurn(gameRoom);

        Assert.assertTrue(new ReflectionEquals(expectedGameRoom).matches(result));
        verify(this.gameRoomRepositoryMock, times(1)).save(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenPlayerIdAndTargetFireWhenCheckIfMissAndConfirmedHitThenReturnFlagTrue() {
        String playerId = "1";
        String targetFire = "6";
        Player player = Player.builder()
                .id("1")
                .name("testPlayer")
                .gameRoomId("11")
                .build();
        GameRoom gameRoom = GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .playerTwoId("2")
                .roomFull(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .onPlay(true)
                .build();
        Player enemyPlayer = Player.builder()
                .id("2")
                .gameRoomId("11")
                .name("testEnemyPLayer")
                .playerShips(Arrays.asList(
                        Ship.builder()
                                .name("destroyer")
                                .position(ShipStatusAndPosition.VERTICAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .damage(0)
                                                .placement(5)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(6)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(7)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        Mockito.when(this.playerServiceMock.getPlayerById("1")).thenReturn(player);
        Mockito.when(this.gameRoomRepositoryMock.findById("11")).thenReturn(Optional.of(gameRoom));
        Mockito.when(this.playerServiceMock.getPlayerById("2")).thenReturn(enemyPlayer);

        boolean result = this.gameRoomService.checkIfMissOrHitAndRecord(playerId, targetFire);

        Assert.assertTrue(result);
        verify(this.playerServiceMock, times(2)).getPlayerById(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).findById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(2)).savePlayer(Mockito.any(Player.class));
    }

    @Test
    public void givenPlayerIdAndTargetFireWhenCheckIfMissAndTargetMissedThenReturnFlagFalse() {
        String playerId = "1";
        String targetFire = "10";
        Player player = Player.builder()
                .id("1")
                .name("testPlayer")
                .gameRoomId("11")
                .build();
        GameRoom gameRoom = GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .playerTwoId("2")
                .roomFull(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .onPlay(true)
                .build();
        Player enemyPlayer = Player.builder()
                .id("2")
                .gameRoomId("11")
                .name("testEnemyPLayer")
                .playerShips(Arrays.asList(
                        Ship.builder()
                                .name("destroyer")
                                .position(ShipStatusAndPosition.VERTICAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .damage(0)
                                                .placement(5)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(6)
                                                .build(),
                                        Placement.builder()
                                                .damage(0)
                                                .placement(7)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        Mockito.when(this.playerServiceMock.getPlayerById("1")).thenReturn(player);
        Mockito.when(this.gameRoomRepositoryMock.findById("11")).thenReturn(Optional.of(gameRoom));
        Mockito.when(this.playerServiceMock.getPlayerById("2")).thenReturn(enemyPlayer);

        boolean result = this.gameRoomService.checkIfMissOrHitAndRecord(playerId, targetFire);

        Assert.assertTrue(!result);
        verify(this.playerServiceMock, times(2)).getPlayerById(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).findById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(1)).savePlayer(Mockito.any(Player.class));
    }

    @Test
    public void givenGameRoomAndTargetFireWhenSetShipFlagAndGetNameThenReturnName() {
        String targetFire = "5";
        GameRoom gameRoom = GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .playerTwoId("2")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .roomFull(true)
                .pause(true)
                .build();
        Player player = Player.builder()
                .id("2")
                .gameRoomId("11")
                .playerShips(Arrays.asList(
                        Ship.builder()
                                .name("destroyer")
                                .status(ShipStatusAndPosition.FUNCTIONAL)
                                .position(ShipStatusAndPosition.HORIZONTAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .placement(3)
                                                .damage(1)
                                                .build(),
                                        Placement.builder()
                                                .placement(4)
                                                .damage(1)
                                                .build(),
                                        Placement.builder()
                                                .placement(5)
                                                .damage(1)
                                                .build()
                                ))
                                .build(),
                        Ship.builder().name("cruiser")
                                .status(ShipStatusAndPosition.FUNCTIONAL)
                                .position(ShipStatusAndPosition.VERTICAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .placement(7)
                                                .damage(0)
                                                .build(),
                                        Placement.builder()
                                                .placement(10)
                                                .damage(0)
                                                .build(),
                                        Placement.builder()
                                                .placement(15)
                                                .damage(0)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        String expected = "destroyer";
        Mockito.when(this.playerServiceMock.getPlayerById("2")).thenReturn(player);

        String result = this.gameRoomService.setShipFlagAndGetNameIfDestroyed(gameRoom, targetFire);

        Assert.assertSame(expected, result);
        verify(this.playerServiceMock, times(1)).getPlayerById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(1)).savePlayer(Mockito.any(Player.class));
    }

    @Test
    public void givenGameRoomAndTargetFireWhenSetShipFlagAndGetNameThenReturnEmptyString() {
        String targetFire = "5";
        GameRoom gameRoom = GameRoom.builder()
                .id("11")
                .playerOneId("1")
                .playerTwoId("2")
                .onPlay(true)
                .currentTurn(TurnStatus.PLAYER_ONE)
                .roomFull(true)
                .pause(true)
                .build();
        Player player = Player.builder()
                .id("2")
                .gameRoomId("11")
                .playerShips(Arrays.asList(
                        Ship.builder()
                                .name("destroyer")
                                .status(ShipStatusAndPosition.FUNCTIONAL)
                                .position(ShipStatusAndPosition.HORIZONTAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .placement(3)
                                                .damage(0)
                                                .build(),
                                        Placement.builder()
                                                .placement(4)
                                                .damage(0)
                                                .build(),
                                        Placement.builder()
                                                .placement(5)
                                                .damage(1)
                                                .build()
                                ))
                                .build(),
                        Ship.builder().name("cruiser")
                                .status(ShipStatusAndPosition.FUNCTIONAL)
                                .position(ShipStatusAndPosition.VERTICAL)
                                .placement(Arrays.asList(
                                        Placement.builder()
                                                .placement(7)
                                                .damage(0)
                                                .build(),
                                        Placement.builder()
                                                .placement(10)
                                                .damage(0)
                                                .build(),
                                        Placement.builder()
                                                .placement(15)
                                                .damage(0)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        String expected = "";
        Mockito.when(this.playerServiceMock.getPlayerById("2")).thenReturn(player);

        String result = this.gameRoomService.setShipFlagAndGetNameIfDestroyed(gameRoom, targetFire);

        Assert.assertSame(expected, result);
        verify(this.playerServiceMock, times(1)).getPlayerById(Mockito.any(String.class));
        verify(this.playerServiceMock, times(0)).savePlayer(Mockito.any(Player.class));
    }

    @Test
    public void givenPlayerIdWhenCheckIfGameOverThenReturnTrue() {
        Player currentPlayer = Player.builder()
                .id("1")
                .gameRoomId("11")
                .build();
        GameRoom gameRoom = GameRoom.builder()
                .id("11")
                .currentTurn(TurnStatus.PLAYER_ONE)
                .playerOneId("1")
                .playerTwoId("2")
                .build();
        Player enemyPlayer = Player.builder()
                .id("2")
                .gameRoomId("11")
                .playerShips(Arrays.asList(
                        Ship.builder()
                                .name("destroyer")
                                .position(ShipStatusAndPosition.HORIZONTAL)
                                .status(ShipStatusAndPosition.DESTROYED)
                                .build(),
                        Ship.builder()
                                .name("cruiser")
                                .position(ShipStatusAndPosition.HORIZONTAL)
                                .status(ShipStatusAndPosition.DESTROYED)
                                .build(),
                        Ship.builder()
                                .name("destroyer")
                                .position(ShipStatusAndPosition.VERTICAL)
                                .status(ShipStatusAndPosition.DESTROYED)
                                .build()
                ))
                .build();
        Mockito.when(this.playerServiceMock.getPlayerById("1")).thenReturn(currentPlayer);
        Mockito.when(this.gameRoomRepositoryMock.findById("11")).thenReturn(Optional.of(gameRoom));
        Mockito.when(this.playerServiceMock.getPlayerById("2")).thenReturn(enemyPlayer);

        boolean result = this.gameRoomService.checkIfGameOver("1");

        Assert.assertTrue(result);
        verify(this.playerServiceMock, times(2)).getPlayerById(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).findById(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).save(Mockito.any(GameRoom.class));
    }

    @Test
    public void givenPlayerIdWhenCheckIfGameOverThenReturnFalse() {
        Player currentPlayer = Player.builder()
                .id("1")
                .gameRoomId("11")
                .build();
        GameRoom gameRoom = GameRoom.builder()
                .id("11")
                .currentTurn(TurnStatus.PLAYER_ONE)
                .playerOneId("1")
                .playerTwoId("2")
                .build();
        Player enemyPlayer = Player.builder()
                .id("2")
                .gameRoomId("11")
                .playerShips(Arrays.asList(
                        Ship.builder()
                                .name("destroyer")
                                .position(ShipStatusAndPosition.HORIZONTAL)
                                .status(ShipStatusAndPosition.FUNCTIONAL)
                                .build(),
                        Ship.builder()
                                .name("cruiser")
                                .position(ShipStatusAndPosition.HORIZONTAL)
                                .status(ShipStatusAndPosition.DESTROYED)
                                .build(),
                        Ship.builder()
                                .name("destroyer")
                                .position(ShipStatusAndPosition.VERTICAL)
                                .status(ShipStatusAndPosition.DESTROYED)
                                .build()
                ))
                .build();
        Mockito.when(this.playerServiceMock.getPlayerById("1")).thenReturn(currentPlayer);
        Mockito.when(this.gameRoomRepositoryMock.findById("11")).thenReturn(Optional.of(gameRoom));
        Mockito.when(this.playerServiceMock.getPlayerById("2")).thenReturn(enemyPlayer);

        boolean result = this.gameRoomService.checkIfGameOver("1");

        Assert.assertTrue(!result);
        verify(this.playerServiceMock, times(2)).getPlayerById(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(1)).findById(Mockito.any(String.class));
        verify(this.gameRoomRepositoryMock, times(0)).save(Mockito.any(GameRoom.class));
    }
}