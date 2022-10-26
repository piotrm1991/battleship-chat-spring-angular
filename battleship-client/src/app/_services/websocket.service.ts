import { Injectable } from '@angular/core';

import * as SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';

const URI_GLOBAL_CHAT = "http://localhost:8081/chat-global";
const URI_LOBBY = "http://localhost:8082/lobby";

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  prepareStompClientGlobalChat(): any {
    const socket = new SockJS(URI_GLOBAL_CHAT);
    const stompClient = Stomp.over(socket);
    return stompClient;
  }

  prepareStompClientLobby(): any {
    const socket = new SockJS(URI_LOBBY);
    const stompClient = Stomp.over(socket);
    return stompClient;
  }

  prepareStompClientGame(): any {
    const socket = new SockJS(URI_LOBBY);
    const stompClient = Stomp.over(socket);
    return stompClient;
  }
}
