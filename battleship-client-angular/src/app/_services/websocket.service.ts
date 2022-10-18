import { Injectable, OnInit } from '@angular/core';
import { Message } from '../_data/message';

import * as SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';

const URI_GLOBAL_CHAT = "http://localhost:8081/chat-global";

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  prepareStompClientGlobalChat(): any {
    const socket = new SockJS(URI_GLOBAL_CHAT);
    const stompClient = Stomp.over(socket);
    return stompClient;
  }
}
