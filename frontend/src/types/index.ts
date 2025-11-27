export interface User {
  id: number;
  clerkId: string;
  displayName: string;
  avatarUrl?: string;
  isAdmin: boolean;
  email?: string;
}

export interface Channel {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
}

export interface Message {
  id: number;
  channelId: number;
  sender: User;
  content: string;
  type: 'TEXT' | 'FILE';
  createdAt: string;
  reactionCount: number;
}

export interface TypingEvent {
  channelId: number;
  clerkId: string;
  displayName: string;
  isTyping: boolean;
}

export interface PresenceEvent {
  channelId: number;
  onlineUsers: string[];
  onlineCount: number;
}
