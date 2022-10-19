import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { StorageService } from '../_services/storage.service';
import { UserService } from '../_services/user.service';
import { WebSocketService } from '../_services/websocket.service';

@Component({
selector: 'app-lobby',
templateUrl: './lobby.component.html',
styleUrls: ['./lobby.component.css']
})
export class LobbyComponent implements OnInit, OnDestroy {
  content?: string;

  private currentUser: any;

  private stompClient: any;

  private gameRoomId: String;

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
      this.userService.getUserBoard().subscribe({
        next: data => {
          this.content = data;
        },
        error: err => {
          if (err.error) {
            try {
              const res = JSON.parse(err.error);
              this.content = res.message;
            } catch {
              this.content = `Error with status: ${err.status} - ${err.statusText}`;
            }
          } else {
            this.content = `Error with status: ${err.status}`;
          }
        }
      });

      this.currentUser = this.storageService.getUser();
      this.stompClient = this.websocketService.prepareStompClientLobby();
      this.connectLobby();
      this.gameRoomId = "";
    }
  }

  ngOnDestroy(): void {
    if (this.storageService.isLoggedIn()) {
      this.stompClient.disconnect();
    }
  }

  connectLobby() {
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
    console.log('Connected: ' + frame);
    this.stompClient.subscribe('/topic/public', (payload) => {
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

    const headerInfo = document.querySelector('#headerInfo');
    
      if (message.type === 'NEW_GAME_ROOM' && message.senderId === this.currentUser.id) {
        this.gameRoomId = message.gameRoomId;
        headerInfo.innerHTML = 'Waiting for player...';
      } else if (message.type === 'GAME_STARTED' && this.gameRoomId === "") {
        headerInfo.innerHTML = 'New Challenge!';
        this.gameRoomId = message.gameRoomId;
        this.onSubscribeToChat();
      } else if (message.type === 'GAME_STARTED' && this.gameRoomId === message.gameRoomId) {
        headerInfo.innerHTML = 'New Challenge!';
        this.onSubscribeToChat();
      }
    
  }

  private onSubscribeToChat() {
    this.stompClient.subscribe('/topic/private/chat/' + this.gameRoomId, (payload) => {
      this.onMessageRecivedChat(payload);
    });
    this.stompClient.send(
      '/app/chat.newPlayer/' + this.gameRoomId,
      {},
      JSON.stringify({
        senderId: this.currentUser.id, 
        sender: this.currentUser.username, 
        type: 'CONNECT', 
        gameRoomId: this.gameRoomId
      })
    );
  }

  sendMessage(sendForm: NgForm) {
    const content = sendForm.value.message;
    this.stompClient.send(
      '/app/chat.send/' + this.gameRoomId,
      {},
      JSON.stringify({sender: this.currentUser.username, content: content, type: 'CHAT', time: moment().format('MMMM Do YYYY, h:mm:ss a')})
    );
    sendForm.control.reset();
  }

  private onMessageRecivedChat(payload) {
    const message = JSON.parse(payload.body);

    const chatCard = document.createElement('div');
    chatCard.className = 'card-body';

    const flexBox = document.createElement('div');
    flexBox.className = 'd-flex justify-content-end mb-4';
    chatCard.appendChild(flexBox);

    const messageElement = document.createElement('div');
    messageElement.className = 'msg_container_send';

    flexBox.appendChild(messageElement);

    if (message.type === 'CONNECT') {
      console.log('CONNECT ' + message.sender);

      messageElement.classList.add('event-message');
      message.content = message.sender + ' connected!';
    } else if (message.type === 'DISCONNECT') {
      console.log('DISCONNECT ' + message.sender);

      messageElement.classList.add('event-message');
      message.content = message.sender + ' left!';
    } else if (message.type === 'CHAT') {
      messageElement.classList.add('chat-message');

      const avatarContainer = document.createElement('div');
      avatarContainer.className = 'img_cont_msg';
      const avatarElement = document.createElement('div');
      avatarElement.className = 'circle user_img_msg';
      const avatarText = document.createTextNode(message.sender[0]);
      avatarElement.appendChild(avatarText);
      avatarElement.style['background-color'] = getAvatarColor(message.sender);
      avatarContainer.appendChild(avatarElement);

      messageElement.style['background-color'] = getAvatarColor(message.sender);

      flexBox.appendChild(avatarContainer);

      const time = document.createElement('span');
      time.className = 'msg_time_send';
      time.innerHTML = message.time;
      messageElement.appendChild(time);

      const sender = document.createElement('span');
      sender.className = 'msg_sender_send';
      sender.innerHTML = message.sender;
      messageElement.appendChild(sender);
    }

    const messageTextContainer = document.createElement('span');
    messageTextContainer.innerHTML = message.content;
    messageElement.appendChild(messageTextContainer);
    
    const chat = document.querySelector('#chat');
    chat.appendChild(flexBox);
    chat.scrollTop = chat.scrollHeight;
  }
}

function hashCode(str) {
  let hash = 0;
    for (let i = 0; i < str.length; i++) {
       hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return hash;
}

function getAvatarColor(sender) {
  const colours = ['#2196F3', '#32c787', '#1BC6B4', '#A1B4C4', '#a6070d', '#0e01e3', '#0493a4', '#dee83f'];
  const index = Math.abs(hashCode(sender) % colours.length);

  return colours[index];
}
