import { useEffect, useRef, useState } from 'react';
import { WebSocketClient } from '@/lib/websocket';
import { Message, TypingEvent, PresenceEvent } from '@/types';

export const useWebSocket = (clerkId: string | null) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<WebSocketClient | null>(null);

  useEffect(() => {
    if (!clerkId) return;

    const client = new WebSocketClient(clerkId);
    clientRef.current = client;

    client.connect(() => {
      setConnected(true);
    });

    return () => {
      client.disconnect();
      setConnected(false);
    };
  }, [clerkId]);

  const subscribeToChannel = (
    channelId: number,
    onMessage: (message: Message) => void,
    onTyping: (event: TypingEvent) => void,
    onPresence: (event: PresenceEvent) => void
  ) => {
    if (clientRef.current) {
      clientRef.current.subscribeToChannel(channelId, onMessage, onTyping, onPresence);
    }
  };

  const sendMessage = (channelId: number, content: string, type: 'TEXT' | 'FILE' = 'TEXT') => {
    if (clientRef.current) {
      clientRef.current.sendMessage(channelId, content, type);
    }
  };

  const sendTyping = (channelId: number, displayName: string, isTyping: boolean) => {
    if (clientRef.current) {
      clientRef.current.sendTyping(channelId, displayName, isTyping);
    }
  };

  const leaveChannel = (channelId: number) => {
    if (clientRef.current) {
      clientRef.current.leaveChannel(channelId);
    }
  };

  return {
    connected,
    subscribeToChannel,
    sendMessage,
    sendTyping,
    leaveChannel,
  };
};
