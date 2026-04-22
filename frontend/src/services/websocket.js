import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient = null;
let onMessageCallback = null;
let onStatusCallback = null;

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

export function connect(userId, onMessage, onStatus) {
  onMessageCallback = onMessage;
  onStatusCallback = onStatus;

  stompClient = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  stompClient.onConnect = () => {
    console.log('WebSocket connected');

    // Subscribe to personal message queue
    stompClient.subscribe(`/user/${userId}/queue/messages`, (msg) => {
      const parsed = JSON.parse(msg.body);
      if (onMessageCallback) onMessageCallback(parsed);
    });

    // Subscribe to status updates
    stompClient.subscribe(`/user/${userId}/queue/status`, (msg) => {
      const parsed = JSON.parse(msg.body);
      if (onStatusCallback) onStatusCallback(parsed);
    });
  };

  stompClient.onStompError = (frame) => {
    console.error('STOMP error:', frame.headers['message']);
  };

  stompClient.activate();
}

export function sendDM(senderId, receiverId, encryptedPayload, encryptedKey) {
  if (!stompClient || !stompClient.connected) return;
  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({ senderId, receiverId, encryptedPayload, encryptedKey }),
  });
}

export function sendGroupMsg(senderId, groupId, encryptedPayload, encryptedKey) {
  if (!stompClient || !stompClient.connected) return;
  stompClient.publish({
    destination: '/app/chat.group',
    body: JSON.stringify({ senderId, groupId, encryptedPayload, encryptedKey }),
  });
}

export function disconnect() {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
}
