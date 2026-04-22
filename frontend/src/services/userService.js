import API from '../api/axios';

export const getAllUsers = () => API.get('/users');

export const getUserById = (userId) => API.get(`/users/${userId}`);

export const searchUsers = (username) => API.get(`/users/search?username=${username}`);

export const getPublicKey = (userId) => API.get(`/users/${userId}/public-key`);

export const updatePublicKey = (userId, publicKey) => {
  return API.put(`/users/${userId}/public-key`, { publicKey });
};

export const checkOnline = (userId) => API.get(`/users/${userId}/online`);
