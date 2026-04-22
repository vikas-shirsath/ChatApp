import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { connect, disconnect } from '../services/websocket';
import { getUserById } from '../services/userService';
import Sidebar from '../components/Sidebar';
import ChatWindow from '../components/ChatWindow';
import CreateGroup from '../components/CreateGroup';
import EmptyState from '../components/EmptyState';
import Toast from '../components/Toast';
import './Chat.css';

export default function Chat() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [selectedChat, setSelectedChat] = useState(null);
  const [incomingMsg, setIncomingMsg] = useState(null);
  const [showCreateGroup, setShowCreateGroup] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [unreadCounts, setUnreadCounts] = useState({});
  const [notifications, setNotifications] = useState([]);
  const selectedChatRef = useRef(null);

  // Keep ref in sync with state so WebSocket callback reads the latest value
  useEffect(() => {
    selectedChatRef.current = selectedChat;
  }, [selectedChat]);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    connect(
      user.userId,
      (msg) => {
        const current = selectedChatRef.current;

        // Check if message belongs to the currently open chat
        let isFromCurrentChat = false;
        if (current) {
          if (current.type === 'group') {
            // Group chat — match by groupId
            isFromCurrentChat = msg.groupId === current.id;
          } else {
            // DM — match by senderId/receiverId
            isFromCurrentChat =
              (msg.senderId === current.id && msg.receiverId === user.userId) ||
              (msg.senderId === user.userId && msg.receiverId === current.id);
          }
        }

        if (isFromCurrentChat) {
          setIncomingMsg(msg);
        } else if (msg.senderId !== user.userId) {
          // Not the open chat — show notification + increment unread
          const unreadKey = msg.groupId || msg.senderId;
          setUnreadCounts((prev) => ({
            ...prev,
            [unreadKey]: (prev[unreadKey] || 0) + 1,
          }));
          showNotification(msg);
        }
      },
      (status) => console.log('Status update:', status)
    );

    return () => disconnect();
  }, [user, navigate]);

  const showNotification = async (msg) => {
    let senderName = 'Someone';
    try {
      const res = await getUserById(msg.senderId);
      senderName = res.data.username;
    } catch (e) { /* ignore */ }

    const id = Date.now() + Math.random();
    setNotifications((prev) => [
      ...prev,
      {
        id,
        senderId: msg.senderId,
        senderName,
        preview: 'Sent you a message',
        senderData: null,
      },
    ]);

    // Also try to get full user data for click-to-open
    try {
      const res = await getUserById(msg.senderId);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, senderData: res.data } : n))
      );
    } catch (e) { /* ignore */ }
  };

  const dismissNotification = (id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const handleNotificationClick = (notification) => {
    dismissNotification(notification.id);
    if (notification.senderData) {
      setSelectedChat({ ...notification.senderData, type: 'user' });
      // Clear unread for this user
      setUnreadCounts((prev) => {
        const next = { ...prev };
        delete next[notification.senderId];
        return next;
      });
    }
  };

  const handleSelectChat = (chat) => {
    setSelectedChat(chat);
    // Clear unread for selected user
    if (chat?.id) {
      setUnreadCounts((prev) => {
        const next = { ...prev };
        delete next[chat.id];
        return next;
      });
    }
  };

  const handleLogout = () => {
    disconnect();
    logout();
    navigate('/login');
  };

  const handleGroupCreated = useCallback(() => {
    setRefreshKey((k) => k + 1);
  }, []);

  if (!user) return null;

  return (
    <div className="chat-layout">
      <Sidebar
        key={refreshKey}
        currentUser={user}
        selectedChat={selectedChat}
        onSelect={handleSelectChat}
        onLogout={handleLogout}
        onCreateGroup={() => setShowCreateGroup(true)}
        unreadCounts={unreadCounts}
      />
      <div className="chat-main">
        {selectedChat ? (
          <ChatWindow
            currentUser={user}
            chat={selectedChat}
            incomingMsg={incomingMsg}
          />
        ) : (
          <EmptyState />
        )}
      </div>

      {/* Toast notifications */}
      <Toast
        notifications={notifications}
        onDismiss={dismissNotification}
        onClickNotification={handleNotificationClick}
      />

      {showCreateGroup && (
        <CreateGroup
          currentUser={user}
          onClose={() => setShowCreateGroup(false)}
          onGroupCreated={handleGroupCreated}
        />
      )}
    </div>
  );
}
