package it.piotrmachnik.gameRoomService.model;

public enum MessageType {
    CONNECT,
    CONNECT_ENEMY,
    DISCONNECT,
    CHAT,
    SET_SHIPS,
    NOT_READY,
    READY,
    YOUR_TURN,
    ENEMY_TURN,
    FIRE,
    AFTER_FIRE,
    DESTROYED_SHIP,
    ENEMY_FIRE,
    GAME_OVER
}
