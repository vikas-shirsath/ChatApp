import { useState, useEffect, useRef } from 'react';
import { getChatHistory, sendMessage } from '../services/messageService';
import { sendGroupMessage, getGroupMessages } from '../services/groupService';
import { getPublicKey } from '../services/userService';
import { encryptMessage, decryptMessage } from '../utils/crypto';
import MessageBubble from './MessageBubble';

function getSentCache() {
  try { return JSON.parse(localStorage.getItem('sentMsgCache') || '{}'); }
  catch { return {}; }
}

function cacheSentMsg(msgId, plaintext) {
  const cache = getSentCache();
  cache[msgId] = plaintext;
  localStorage.setItem('sentMsgCache', JSON.stringify(cache));
}

function looksLikeBase64(str) {
  if (!str || str.length < 20) return false;
  return /^[A-Za-z0-9+/=]+$/.test(str);
}

export default function ChatWindow({ currentUser, chat, incomingMsg }) {
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [receiverPubKey, setReceiverPubKey] = useState(null);
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);
  const inputRef = useRef(null);

  const privateKey = localStorage.getItem('privateKey');
  const isGroup = chat?.type === 'group';

  // Load chat history on chat change
  useEffect(() => {
    if (!chat) return;
    setMessages([]);
    setReceiverPubKey(null);
    inputRef.current?.focus();

    if (isGroup) {
      // Load group messages
      getGroupMessages(chat.id)
        .then(async (res) => {
          const decrypted = await Promise.all(
            res.data.map(async (msg) => ({ ...msg, plaintext: await getPlaintext(msg) }))
          );
          setMessages(decrypted);
        })
        .catch((err) => console.error('Group history load failed:', err));
    } else {
      // Load DM – fetch receiver's public key + history
      getPublicKey(chat.id)
        .then((res) => setReceiverPubKey(res.data.publicKey || null))
        .catch(() => {});

      getChatHistory(chat.id, currentUser.userId)
        .then(async (res) => {
          const decrypted = await Promise.all(
            res.data.map(async (msg) => ({ ...msg, plaintext: await getPlaintext(msg) }))
          );
          setMessages(decrypted);
        })
        .catch((err) => console.error('History load failed:', err));
    }
  }, [chat, currentUser.userId]);

  // Real-time incoming messages
  useEffect(() => {
    if (!incomingMsg || !chat) return;

    let isForThisChat = false;
    if (isGroup) {
      isForThisChat = incomingMsg.groupId === chat.id;
    } else {
      isForThisChat =
        (incomingMsg.senderId === chat.id && incomingMsg.receiverId === currentUser.userId) ||
        (incomingMsg.senderId === currentUser.userId && incomingMsg.receiverId === chat.id);
    }

    if (isForThisChat) {
      (async () => {
        const plaintext = await getPlaintext(incomingMsg);
        setMessages((prev) => {
          if (prev.some((m) => m.id === incomingMsg.id)) return prev;
          return [...prev, { ...incomingMsg, plaintext }];
        });
      })();
    }
  }, [incomingMsg]);

  async function getPlaintext(msg) {
    const payload = msg.encryptedPayload;

    // Check sent cache first
    if (msg.senderId === currentUser.userId) {
      const cached = getSentCache()[msg.id];
      if (cached) return cached;
    }

    // Try E2EE decryption for DM
    if (privateKey && msg.encryptedKey && msg.encryptedKey !== 'plaintext-key' && msg.encryptedKey !== '') {
      try {
        const decrypted = await decryptMessage(payload, msg.encryptedKey, privateKey);
        if (decrypted) return decrypted;
      } catch (e) { /* fall through */ }
    }

    // If it doesn't look encrypted, show as-is
    if (payload && !looksLikeBase64(payload)) return payload;

    return msg.senderId === currentUser.userId ? '[Sent message]' : '[Encrypted]';
  }

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!text.trim() || sending) return;

    const plaintext = text.trim();
    setText('');
    setSending(true);

    try {
      let res;

      if (isGroup) {
        // Group message – send plaintext (group E2EE not yet implemented)
        res = await sendGroupMessage(chat.id, currentUser.userId, plaintext, '');
      } else {
        // DM – encrypt if public key available
        let payload, key;
        if (receiverPubKey) {
          const encrypted = await encryptMessage(plaintext, receiverPubKey);
          payload = encrypted.encryptedPayload;
          key = encrypted.encryptedKey;
        } else {
          payload = plaintext;
          key = '';
        }
        res = await sendMessage(currentUser.userId, chat.id, payload, key);
      }

      cacheSentMsg(res.data.id, plaintext);
      setMessages((prev) => [...prev, { ...res.data, plaintext }]);
    } catch (err) {
      console.error('Send failed:', err);
    } finally {
      setSending(false);
      inputRef.current?.focus();
    }
  };

  const chatName = isGroup ? chat.name : chat.username;

  return (
    <div className="chat-window">
      {/* Top bar */}
      <div className="chat-topbar">
        <div className={`avatar ${isGroup ? 'group-avatar' : ''}`}>
          {(chatName || '?')[0].toUpperCase()}
          {!isGroup && chat.online && <div className="online-dot" />}
        </div>
        <div className="chat-topbar-info">
          <div className="chat-topbar-name">{chatName}</div>
          <div className="chat-topbar-status">
            {isGroup && <span>{chat.memberCount} members</span>}
            {!isGroup && chat.online && <span className="online-text">Online</span>}
            {!isGroup && !chat.online && <span>Offline</span>}
            {!isGroup && receiverPubKey && <span className="encrypted-badge">🔒 E2EE</span>}
          </div>
        </div>
      </div>

      {/* Messages */}
      <div className="messages-area">
        {messages.length === 0 && (
          <div className="messages-empty">
            <span className="emoji">{isGroup ? '👥' : '👋'}</span>
            <p>{isGroup ? `Start chatting in ${chatName}` : `Start a conversation with ${chatName}`}</p>
          </div>
        )}

        {messages.map((msg, idx) => (
          <MessageBubble
            key={msg.id || idx}
            message={msg}
            isMine={msg.senderId === currentUser.userId}
          />
        ))}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <form className="input-area" onSubmit={handleSend}>
        <div className="input-wrapper">
          <input
            ref={inputRef}
            type="text"
            placeholder={isGroup ? 'Type a group message...' : (receiverPubKey ? '🔒 Type an encrypted message...' : 'Type a message...')}
            value={text}
            onChange={(e) => setText(e.target.value)}
            autoFocus
          />
        </div>
        <button type="submit" className="btn-send" disabled={!text.trim() || sending}>
          ➤
        </button>
      </form>
    </div>
  );
}
