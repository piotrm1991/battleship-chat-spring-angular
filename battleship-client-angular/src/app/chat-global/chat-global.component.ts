import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';

import { StorageService } from '../_services/storage.service';
import { WebSocketService } from '../_services/websocket.service';

import { NgForm } from '@angular/forms';
import * as moment from 'moment';

@Component({
  selector: 'app-chat-global',
  encapsulation: ViewEncapsulation.None,
  templateUrl: './chat-global.component.html',
  styleUrls: ['./chat-global.component.css']
})
export class ChatGlobalComponent implements OnInit, OnDestroy {

  public content: any;

  private currentUser: any;

  private stompClient: any;

  constructor(private storageService: StorageService, private websocketService: WebSocketService){ }

  ngOnInit() {
    this.currentUser = this.storageService.getUser();
    this.stompClient = this.websocketService.prepareStompClientGlobalChat();
    this.connect(this.currentUser);
  }
  
  ngOnDestroy(): void {
    this.stompClient.disconnect();
  }

  connect(currentUser) {
    const _this = this;
    this.stompClient.connect({}, (frame) => {
      
      this.onConnected(frame, currentUser);
    }, (frame) => {
      this.onError(frame);
    });
  }

  private onConnected(frame: any, currentUser: any) {
    console.log('Connected: ' + frame);
    this.stompClient.subscribe('/topic/public', (payload) => {
      this.onMessageRecived(payload);
    });
    this.stompClient.send(
      '/app/chat.newUser',
      {},
      JSON.stringify({sender: currentUser.username, type: 'CONNECT'})
    );
  }

  private onMessageRecived(payload) {
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
      console.log('CHAT ' + message.content);

      messageElement.classList.add('chat-message');


      const avatarContainer = document.createElement('div');
      avatarContainer.className = 'img_cont_msg';
      const avatarElement = document.createElement('div');
      avatarElement.className = 'circle user_img_msg';
      const avatarText = document.createTextNode(message.sender[0]);
      avatarElement.appendChild(avatarText);
      avatarElement.style['background-color'] = this.getAvatarColor(message.sender);
      avatarContainer.appendChild(avatarElement);

      messageElement.style['background-color'] = this.getAvatarColor(message.sender);

      flexBox.appendChild(avatarContainer);

      const time = document.createElement('span');
      time.className = 'msg_time_send';
      time.innerHTML = message.time;
      messageElement.appendChild(time);
      
    }

    const messageTextContainer = document.createElement('span');
    messageTextContainer.innerHTML = message.content;
    messageElement.appendChild(messageTextContainer);
    

    const chat = document.querySelector('#chat');
    chat.appendChild(flexBox);
    chat.scrollTop = chat.scrollHeight;
  }

  private getAvatarColor(sender) {
    const colours = ['#2196F3', '#32c787', '#1BC6B4', '#A1B4C4']
    const index = Math.abs(hashCode(sender) % colours.length)
    return colours[index]
  }

  sendMessage(sendForm: NgForm) {
    const content = sendForm.value.message;
    this.stompClient.send(
      '/app/chat.send',
      {},
      JSON.stringify({sender: this.currentUser.username, content: content, type: 'CHAT', time: moment().format('MMMM Do YYYY, h:mm:ss a')})
    );
    sendForm.control.reset();
  }

  private onError(frame) {
    console.log("CONNECTION ERROR: " + frame);
  }
}

function hashCode(str) {
  let hash = 0
    for (let i = 0; i < str.length; i++) {
       hash = str.charCodeAt(i) + ((hash << 5) - hash)
    }
    return hash
}


