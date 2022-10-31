package it.piotrmachnik.gameRoomService.service;

import it.piotrmachnik.gameRoomService.model.gameRoom.player.Player;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerGameStatusType;
import it.piotrmachnik.gameRoomService.model.gameRoom.player.PlayerOnlineStatusType;
import it.piotrmachnik.gameRoomService.repository.PlayerRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PlayerServiceMockTest {

    private PlayerService playerService;

    @Mock
    private PlayerRepository playerRepositoryMock;

    @Mock
    private UserService userServiceMock;

    @Before
    public void setup() {
        this.playerService = new PlayerService(playerRepositoryMock, userServiceMock);
    }

    @Test
    public void givenUserIdWhenSetPlayerOnlineThenReturnPlayerOnline() {
        // Given
        Optional<Player> returnPlayerById = Optional.ofNullable(
                Player.builder()
                        .id("1")
                        .name("testPlayer")
                        .playerOnlineStatus(PlayerOnlineStatusType.OFFLINE)
                        .playerGameStatus(PlayerGameStatusType.NOT_READY)
                        .build());
        Player expected = Player.builder()
                .id("1")
                .name("testPlayer")
                .playerOnlineStatus(PlayerOnlineStatusType.ONLINE)
                .playerGameStatus(PlayerGameStatusType.NOT_READY)
                .build();
        Mockito.when(this.playerRepositoryMock.findById("1")).thenReturn(returnPlayerById);
        // When
        Player result = this.playerService.setPlayerOnlineOrInitNewPlayer("1");
        // Then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
    }

    @Test
    public void givenUserIdWhenSetPlayerOnlineThenReturnNewPlayerOnline() {
        // Given
        Optional<Player> returnPlayerById = Optional.empty();
        Player expected = Player.builder()
                .id("1")
                .name("testPlayer")
                .playerOnlineStatus(PlayerOnlineStatusType.ONLINE)
                .playerGameStatus(PlayerGameStatusType.NOT_READY)
                .build();
        Mockito.when(this.playerRepositoryMock.findById("1")).thenReturn(returnPlayerById);
        Mockito.when(this.userServiceMock.getUserName("1")).thenReturn("testPlayer");
        // When
        Player result = this.playerService.setPlayerOnlineOrInitNewPlayer("1");
        // Then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
    }

    @Test
    public void givenPlayerIdWhenSetPlayerOfflineThenReturnPlayerOffline() {
        // Given
        Optional<Player> returnPlayerById = Optional.ofNullable(
                Player.builder()
                        .id("1")
                        .name("testPlayer")
                        .playerOnlineStatus(PlayerOnlineStatusType.ONLINE)
                        .playerGameStatus(PlayerGameStatusType.NOT_READY)
                        .build());
        Player expected = Player.builder()
                .id("1")
                .name("testPlayer")
                .playerOnlineStatus(PlayerOnlineStatusType.OFFLINE)
                .playerGameStatus(PlayerGameStatusType.NOT_READY)
                .build();
        Mockito.when(this.playerRepositoryMock.findById("1")).thenReturn(returnPlayerById);
        // When
        Player result = this.playerService.setPlayerOffline("1");
        // Then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
    }

    @Test
    public void assertSavePlayer() {
        Player expected = Player.builder()
                .id("1")
                .name("testPlayer")
                .playerOnlineStatus(PlayerOnlineStatusType.OFFLINE)
                .playerGameStatus(PlayerGameStatusType.NOT_READY)
                .build();
        Mockito.when(this.playerRepositoryMock.save(Mockito.any(Player.class))).thenReturn(expected);
        Player result = this.playerService.savePlayer(expected);
        verify(this.playerRepositoryMock, times(1)).save(Mockito.any(Player.class));
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
    }

    @Test
    public void givenPlayerIdAndGameRoomIdWhenSetGameRoomIdForPlayerReturnPlayer() {
        // Given
        String currentPlayerId = "1";
        String gameRoomId = "1";
        Optional<Player> player = Optional.ofNullable(Player.builder()
                .id("1")
                .name("testPlayer")
                .build());
        Player expected = Player.builder()
                .id("1")
                .name("testPlayer")
                .gameRoomId("1")
                .build();
        Mockito.when(this.playerRepositoryMock.findById("1")).thenReturn(player);
        // When
        Player result = this.playerService.setGameRoomIdForPlayer(currentPlayerId, gameRoomId);
        //then
        Assert.assertTrue(new ReflectionEquals(expected).matches(result));
    }
}