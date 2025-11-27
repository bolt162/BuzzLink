'use client';

import { useEffect, useState, useRef } from 'react';
import { useUser } from '@clerk/nextjs';
import { Channel, Message, TypingEvent, PresenceEvent } from '@/types';
import { getMessages } from '@/lib/api';
import { useWebSocket } from '@/hooks/useWebSocket';
import MessageList from './MessageList';
import MessageInput from './MessageInput';
import TypingIndicator from './TypingIndicator';
import PresenceIndicator from './PresenceIndicator';

interface ChatPanelProps {
  channel: Channel | null;
}

export default function ChatPanel({ channel }: ChatPanelProps) {
  const { user } = useUser();
  const [messages, setMessages] = useState<Message[]>([]);
  const [typingUsers, setTypingUsers] = useState<Map<string, string>>(new Map());
  const [onlineCount, setOnlineCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutsRef = useRef<Map<string, NodeJS.Timeout>>(new Map());

  const { connected, subscribeToChannel, sendMessage, sendTyping, leaveChannel } = useWebSocket(
    user?.id || null
  );

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (!channel || !connected || !user) return;

    const loadMessages = async () => {
      setLoading(true);
      try {
        const fetchedMessages = await getMessages(channel.id);
        setMessages(fetchedMessages.reverse()); // Chronological order
      } catch (error) {
        console.error('Error loading messages:', error);
      } finally {
        setLoading(false);
      }
    };

    loadMessages();

    const handleNewMessage = (message: Message) => {
      setMessages((prev) => [...prev, message]);
    };

    const handleTyping = (event: TypingEvent) => {
      if (event.clerkId === user.id) return; // Ignore own typing

      const timeout = typingTimeoutsRef.current.get(event.clerkId);
      if (timeout) clearTimeout(timeout);

      if (event.isTyping) {
        setTypingUsers((prev) => {
          const next = new Map(prev);
          next.set(event.clerkId, event.displayName);
          return next;
        });

        const newTimeout = setTimeout(() => {
          setTypingUsers((prev) => {
            const next = new Map(prev);
            next.delete(event.clerkId);
            return next;
          });
        }, 3000);

        typingTimeoutsRef.current.set(event.clerkId, newTimeout);
      } else {
        setTypingUsers((prev) => {
          const next = new Map(prev);
          next.delete(event.clerkId);
          return next;
        });
      }
    };

    const handlePresence = (event: PresenceEvent) => {
      setOnlineCount(event.onlineCount);
    };

    subscribeToChannel(channel.id, handleNewMessage, handleTyping, handlePresence);

    return () => {
      leaveChannel(channel.id);
      typingTimeoutsRef.current.forEach(clearTimeout);
      typingTimeoutsRef.current.clear();
    };
  }, [channel, connected, user]);

  const handleSendMessage = (content: string, type: 'TEXT' | 'FILE') => {
    if (!channel) return;
    sendMessage(channel.id, content, type);
  };

  const handleTyping = (isTyping: boolean) => {
    if (!channel || !user) return;
    sendTyping(channel.id, user.fullName || user.username || 'User', isTyping);
  };

  const handleMessageDeleted = (messageId: number) => {
    setMessages((prev) => prev.filter((msg) => msg.id !== messageId));
  };

  const handleReactionToggled = (messageId: number, newCount: number) => {
    setMessages((prev) =>
      prev.map((msg) => (msg.id === messageId ? { ...msg, reactionCount: newCount } : msg))
    );
  };

  if (!channel) {
    return (
      <div className="flex-1 flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-gray-700 mb-2">Welcome to BuzzLink!</h2>
          <p className="text-gray-500">Select a channel to start chatting</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col bg-white">
      <div className="border-b border-gray-200 px-6 py-4">
        <h2 className="text-xl font-semibold text-gray-800">#{channel.name}</h2>
        {channel.description && <p className="text-sm text-gray-500">{channel.description}</p>}
      </div>

      <PresenceIndicator onlineCount={onlineCount} />

      {loading ? (
        <div className="flex-1 flex items-center justify-center">
          <div className="text-gray-500">Loading messages...</div>
        </div>
      ) : (
        <>
          <MessageList
            messages={messages}
            onMessageDeleted={handleMessageDeleted}
            onReactionToggled={handleReactionToggled}
          />
          <div ref={messagesEndRef} />
        </>
      )}

      <TypingIndicator typingUsers={typingUsers} />

      <MessageInput onSendMessage={handleSendMessage} onTyping={handleTyping} />
    </div>
  );
}
