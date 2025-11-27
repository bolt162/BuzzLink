import axios from 'axios';
import { Channel, Message, User } from '@/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add Clerk user ID to requests
export const setClerkUserId = (clerkId: string) => {
  api.defaults.headers.common['X-Clerk-User-Id'] = clerkId;
};

// Channel APIs
export const getChannels = async (): Promise<Channel[]> => {
  const response = await api.get('/api/channels');
  return response.data;
};

export const getChannel = async (id: number): Promise<Channel> => {
  const response = await api.get(`/api/channels/${id}`);
  return response.data;
};

// Message APIs
export const getMessages = async (channelId: number, limit = 50): Promise<Message[]> => {
  const response = await api.get(`/api/channels/${channelId}/messages`, {
    params: { limit },
  });
  return response.data;
};

export const deleteMessage = async (messageId: number): Promise<void> => {
  await api.delete(`/api/messages/${messageId}`);
};

export const toggleReaction = async (messageId: number): Promise<{ count: number }> => {
  const response = await api.post(`/api/messages/${messageId}/reactions`);
  return response.data;
};

// User APIs
export const syncUser = async (
  clerkId: string,
  displayName: string,
  email: string,
  avatarUrl?: string
): Promise<User> => {
  const response = await api.post('/api/users/sync', {
    clerkId,
    displayName,
    email,
    avatarUrl,
  });
  return response.data;
};

export const getCurrentUser = async (): Promise<User> => {
  const response = await api.get('/api/users/me');
  return response.data;
};

export const updateProfile = async (
  displayName: string,
  avatarUrl: string
): Promise<User> => {
  const response = await api.put('/api/users/me', {
    displayName,
    avatarUrl,
  });
  return response.data;
};

export default api;
