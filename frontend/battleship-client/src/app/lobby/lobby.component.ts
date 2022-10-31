import { Component, OnDestroy, OnInit, ViewEncapsulation, AfterViewInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { BattleshipGameComponent } from '../battleship-game/battleship-game.component';
import { ChatPrivateComponent } from '../chat-private/chat-private.component';
import { StorageService } from '../_services/storage.service';
import { UserService } from '../_services/user.service';
import { WebSocketService } from '../_services/websocket.service';

@Component({
selector: 'app-lobby',
templateUrl: './lobby.component.html',
styleUrls: ['./lobby.component.css']
})
export class LobbyComponent implements OnInit, OnDestroy {

  @ViewChild(ChatPrivateComponent) private chatPrivate: ChatPrivateComponent;
  @ViewChild(BattleshipGameComponent) private battleshipGame: BattleshipGameComponent;

  private currentUser: any;

  private stompClient: any;
  
  constructor(private userService: UserService, 
    private storageService: StorageService, 
    private websocketService: WebSocketService,
    private router: Router) {
      if (!storageService.isLoggedIn()) {
        router.navigate(['']);
      }
    }

  ngOnInit(): void {
    if (this.storageService.isLoggedIn()) {
      this.currentUser = this.storageService.getUser();
      this.stompClient = this.websocketService.prepareStompClientLobby();
      this.connectLobby();
    }
  }

  ngOnDestroy(): void {
    this.stompClient.disconnect();
  }

  private connectLobby() {
    this.stompClient.connect({}, (frame) => {
      this.onConnectedLobby(frame);
    }, (frame) => {
      this.onError(frame);
    });
  }

  private onError(frame) {
    console.log("CONNECTION ERROR: " + frame);
  }

  private onConnectedLobby(frame: any) {
    this.stompClient.subscribe('/topic/private/' + this.currentUser.id, (payload) => {
      this.onMessageRecivedLobby(frame, payload);
    });
    this.stompClient.send(
      '/app/lobby.newPlayer',
      {},
      JSON.stringify({
        senderId: this.currentUser.id, 
        type: 'CONNECT'})
    );
  }

  private onMessageRecivedLobby(frame, payload) {
    const message = JSON.parse(payload.body);
      if (message.type === 'CONNECT' && message.senderId === this.currentUser.id) {
      this.chatPrivate.onSubscribeToChat(this.stompClient, message.gameRoomId);
      this.battleshipGame.onSubscribeToGame(this.stompClient);
    } 
  }
}