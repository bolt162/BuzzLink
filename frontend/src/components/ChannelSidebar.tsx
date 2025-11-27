'use client';

import { Channel } from '@/types';

interface ChannelSidebarProps {
  channels: Channel[];
  selectedChannelId: number | null;
  onSelectChannel: (channelId: number) => void;
}

export default function ChannelSidebar({
  channels,
  selectedChannelId,
  onSelectChannel,
}: ChannelSidebarProps) {
  return (
    <div className="w-64 bg-gray-800 text-white flex flex-col h-full">
      <div className="p-4 border-b border-gray-700">
        <h2 className="text-lg font-semibold">Channels</h2>
      </div>
      <div className="flex-1 overflow-y-auto">
        {channels.map((channel) => (
          <button
            key={channel.id}
            onClick={() => onSelectChannel(channel.id)}
            className={`w-full text-left px-4 py-3 hover:bg-gray-700 transition ${
              selectedChannelId === channel.id ? 'bg-gray-700 border-l-4 border-blue-500' : ''
            }`}
          >
            <div className="flex items-center space-x-2">
              <span className="text-gray-400">#</span>
              <span className="font-medium">{channel.name}</span>
            </div>
            {channel.description && (
              <p className="text-xs text-gray-400 mt-1 ml-5">{channel.description}</p>
            )}
          </button>
        ))}
      </div>
    </div>
  );
}
