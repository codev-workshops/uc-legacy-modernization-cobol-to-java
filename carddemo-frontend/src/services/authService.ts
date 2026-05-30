import api from './api';
import type { AuthResponse, LoginCredentials, User } from '../types';

export const authService = {
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const { data } = await api.post<AuthResponse>(
      '/auth/login',
      credentials,
    );
    return data;
  },

  refresh: async (refreshToken: string): Promise<AuthResponse> => {
    const { data } = await api.post<AuthResponse>('/auth/refresh', {
      refreshToken,
    });
    return data;
  },

  getUsers: async (): Promise<User[]> => {
    const { data } = await api.get<User[]>('/users');
    return data;
  },

  getUser: async (userId: string): Promise<User> => {
    const { data } = await api.get<User>(`/users/${userId}`);
    return data;
  },

  createUser: async (
    user: Omit<User, 'userId'> & { password: string },
  ): Promise<User> => {
    const { data } = await api.post<User>('/users', user);
    return data;
  },

  updateUser: async (
    userId: string,
    user: Partial<User & { password: string }>,
  ): Promise<User> => {
    const { data } = await api.put<User>(`/users/${userId}`, user);
    return data;
  },

  deleteUser: async (userId: string): Promise<void> => {
    await api.delete(`/users/${userId}`);
  },
};
