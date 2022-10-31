import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import * as moment from 'moment';
import { StorageService } from '../_services/storage.service';
import { UserService } from '../_services/user.service';
import { WebSocketService } from '../_services/websocket.service';

@Component({
  selector: 'app-chat-private',
  templateUrl: './chat-private.component.html',
  styleUrls: ['./chat-private.component.css']
})
export class ChatPrivateComponent implements OnInit, OnDestroy {

  private currentUser: any;

  private stompClient: any;

  private gameRoomId: String;

  constructor(private userService: UserService, 
    private storageService: StorageService, 
    private websocketService: WebSocketService) { }

  ngOnDestroy(): void {
    // this.stompClient.disconnect();
  }

  ngOnInit(): void {
    if (this.storageService.isLoggedIn()) {
      this.currentUser = this.storageService.getUser();
      this.stompClient = this.websocketService.prepareStompClientLobby();
      this.gameRoomId = "";
    }
  }

  public onSubscribeToChat(stompClient, gameRoomId) {
    this.stompClient = stompClient;
    this.gameRoomId = gameRoomId;
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

      // messageElement.classList.add('event-message');
      // message.content = message.sender + ' connected!';
    } else if (message.type === 'DISCONNECT') {
    //   console.log('DISCONNECT ' + message.sender);

    //   messageElement.classList.add('event-message');
    //   message.content = message.sender + ' left!';
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
