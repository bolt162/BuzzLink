'use client';

import { Message } from '@/types';
import { useUser } from '@clerk/nextjs';
import { deleteMessage, toggleReaction } from '@/lib/api';
import { useState } from 'react';

interface MessageListProps {
  messages: Message[];
  onMessageDeleted: (messageId: number) => void;
  onReactionToggled: (messageId: number, newCount: number) => void;
}

export default function MessageList({
  messages,
  onMessageDeleted,
  onReactionToggled,
}: MessageListProps) {
  const { user } = useUser();
  const [loadingReaction, setLoadingReaction] = useState<number | null>(null);

  const isAdmin = user?.publicMetadata?.isAdmin as boolean;

  const handleDelete = async (messageId: number) => {
    if (!confirm('Are you sure you want to delete this message?')) return;

    try {
      await deleteMessage(messageId);
      onMessageDeleted(messageId);
    } catch (error) {
      console.error('Error deleting message:', error);
      alert('Failed to delete message');
    }
  };

  const handleReaction = async (messageId: number) => {
    if (loadingReaction) return;

    setLoadingReaction(messageId);
    try {
      const result = await toggleReaction(messageId);
      onReactionToggled(messageId, result.count);
    } catch (error) {
      console.error('Error toggling reaction:', error);
    } finally {
      setLoadingReaction(null);
    }
  };

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-4">
      {messages.map((message) => (
        <div
          key={message.id}
          className="flex items-start space-x-3 group hover:bg-gray-50 p-2 rounded"
        >
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-purple-400 to-pink-400 flex items-center justify-center text-white font-semibold flex-shrink-0">
            {message.sender.displayName.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-baseline space-x-2">
              <span className="font-semibold text-gray-900">
                {message.sender.displayName}
              </span>
              {message.sender.isAdmin && (
                <span className="bg-yellow-400 text-black text-xs px-1 rounded">Admin</span>
              )}
              <span className="text-xs text-gray-500">{formatTime(message.createdAt)}</span>
            </div>
            <div className="mt-1">
              {message.type === 'FILE' ? (
                <a
                  href={message.content}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:underline flex items-center space-x-1"
                >
                  <span>📎</span>
                  <span>{message.content}</span>
                </a>
              ) : (
                <p className="text-gray-800 break-words">{message.content}</p>
              )}
            </div>
            <div className="flex items-center space-x-2 mt-2">
              <button
                onClick={() => handleReaction(message.id)}
                disabled={loadingReaction === message.id}
                className={`flex items-center space-x-1 px-2 py-1 rounded hover:bg-gray-200 transition ${
                  loadingReaction === message.id ? 'opacity-50 cursor-not-allowed' : ''
                }`}
              >
                <span>👍</span>
                {message.reactionCount > 0 && (
                  <span className="text-sm text-gray-600">{message.reactionCount}</span>
                )}
              </button>
            </div>
          </div>
          {isAdmin && (
            <button
              onClick={() => handleDelete(message.id)}
              className="opacity-0 group-hover:opacity-100 text-red-500 hover:text-red-700 text-sm transition"
              title="Delete message"
            >
              🗑️
            </button>
          )}
        </div>
      ))}
    </div>
  );
}
