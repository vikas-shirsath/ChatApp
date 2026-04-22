import API from '../api/axios';

export const sendMessage = (senderId, receiverId, encryptedPayload, encryptedKey) => {
  return API.post('/messages/send', {
    senderId,
    receiverId,
    encryptedPayload,
    encryptedKey,
  });
};

export const getChatHistory = (otherUserId, currentUserId) => {
  return API.get(`/messages/${otherUserId}?currentUserId=${currentUserId}`);
};

export const updateMessageStatus = (messageId, status) => {
  return API.patch(`/messages/${messageId}/status`, { messageId, status });
};

export const getUndelivered = (userId) => {
  return API.get(`/messages/undelivered/${userId}`);
};

export const getConversations = (userId) => {
  return API.get(`/messages/conversations/${userId}`);
};
