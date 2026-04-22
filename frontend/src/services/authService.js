import API from '../api/axios';

export const register = (username, email, password) => {
  return API.post('/auth/register', { username, email, password });
};

export const login = (username, password) => {
  return API.post('/auth/login', { username, password });
};
