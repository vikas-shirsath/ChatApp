import { useEffect } from 'react';

export default function Toast({ notifications, onDismiss, onClickNotification }) {
  return (
    <div className="toast-container">
      {notifications.map((n) => (
        <ToastItem
          key={n.id}
          notification={n}
          onDismiss={() => onDismiss(n.id)}
          onClick={() => onClickNotification(n)}
        />
      ))}
    </div>
  );
}

function ToastItem({ notification, onDismiss, onClick }) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, 4000);
    return () => clearTimeout(timer);
  }, [onDismiss]);

  return (
    <div className="toast" onClick={onClick}>
      <div className="toast-header">
        <span className="toast-sender">{notification.senderName || 'New message'}</span>
        <span className="toast-time">now</span>
      </div>
      <div className="toast-body">{notification.preview || 'Sent you a message'}</div>
    </div>
  );
}
