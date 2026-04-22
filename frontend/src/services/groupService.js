import API from '../api/axios';

export const createGroup = (name, createdBy, memberIds) => {
  return API.post('/groups', { name, createdBy, memberIds });
};

export const getUserGroups = (userId) => {
  return API.get(`/groups/user/${userId}`);
};

export const addMember = (groupId, userId, encryptedGroupKey) => {
  return API.post(`/groups/${groupId}/members`, { userId, encryptedGroupKey });
};

export const removeMember = (groupId, userId) => {
  return API.delete(`/groups/${groupId}/members/${userId}`);
};

export const sendGroupMessage = (groupId, senderId, encryptedPayload, encryptedKey) => {
  return API.post(`/groups/${groupId}/messages`, {
    senderId,
    encryptedPayload,
    encryptedKey,
  });
};

export const getGroupMessages = (groupId) => {
  return API.get(`/groups/${groupId}/messages`);
};

export const storeGroupKeys = (groupId, keys) => {
  return API.post(`/groups/${groupId}/keys`, keys);
};

export const getGroupKeys = (groupId) => {
  return API.get(`/groups/${groupId}/keys`);
};
