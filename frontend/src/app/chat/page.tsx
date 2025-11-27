'use client';

import { useEffect, useState } from 'react';
import { useUser } from '@clerk/nextjs';
import { Channel } from '@/types';
import { getChannels, syncUser, setClerkUserId } from '@/lib/api';
import Header from '@/components/Header';
import ChannelSidebar from '@/components/ChannelSidebar';
import ChatPanel from '@/components/ChatPanel';

export default function ChatPage() {
  const { user, isLoaded } = useUser();
  const [channels, setChannels] = useState<Channel[]>([]);
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoaded || !user) return;

    const initialize = async () => {
      try {
        // Sync user with backend
        await syncUser(
          user.id,
          user.fullName || user.username || 'User',
          user.primaryEmailAddress?.emailAddress || '',
          user.imageUrl
        );

        // Set Clerk user ID for API requests
        setClerkUserId(user.id);

        // Load channels
        const fetchedChannels = await getChannels();
        setChannels(fetchedChannels);

        // Select first channel by default
        if (fetchedChannels.length > 0) {
          setSelectedChannel(fetchedChannels[0]);
        }
      } catch (error) {
        console.error('Error initializing chat:', error);
      } finally {
        setLoading(false);
      }
    };

    initialize();
  }, [isLoaded, user]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-500">Loading...</div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col">
      <Header />
      <div className="flex-1 flex overflow-hidden">
        <ChannelSidebar
          channels={channels}
          selectedChannelId={selectedChannel?.id || null}
          onSelectChannel={(id) => {
            const channel = channels.find((c) => c.id === id);
            if (channel) setSelectedChannel(channel);
          }}
        />
        <ChatPanel channel={selectedChannel} />
      </div>
    </div>
  );
}
