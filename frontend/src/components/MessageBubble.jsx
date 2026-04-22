export default function MessageBubble({ message, isMine }) {
  const time = message.timestamp
    ? new Date(message.timestamp).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
      })
    : '';

  const statusIcon = isMine && message.status
    ? message.status === 'READ'
      ? '✓✓'
      : message.status === 'DELIVERED'
      ? '✓✓'
      : '✓'
    : null;

  return (
    <div className={`msg-row ${isMine ? 'sent' : 'received'}`}>
      <div className="msg-bubble">
        {message.plaintext || message.encryptedPayload}
      </div>
      <div className="msg-meta">
        <span>{time}</span>
        {statusIcon && (
          <span className={`check ${message.status === 'READ' ? 'read' : ''}`}>
            {statusIcon}
          </span>
        )}
      </div>
    </div>
  );
}
