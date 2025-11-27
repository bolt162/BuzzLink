'use client';

import { TypingEvent } from '@/types';
import { useEffect, useState } from 'react';

interface TypingIndicatorProps {
  typingUsers: Map<string, string>; // clerkId -> displayName
}

export default function TypingIndicator({ typingUsers }: TypingIndicatorProps) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(typingUsers.size > 0);
  }, [typingUsers]);

  if (!visible) return null;

  const userNames = Array.from(typingUsers.values());
  const text =
    userNames.length === 1
      ? `${userNames[0]} is typing...`
      : userNames.length === 2
      ? `${userNames[0]} and ${userNames[1]} are typing...`
      : `${userNames.length} people are typing...`;

  return (
    <div className="px-4 py-2 text-sm text-gray-500 italic">
      {text}
      <span className="inline-flex ml-1">
        <span className="animate-bounce">.</span>
        <span className="animate-bounce" style={{ animationDelay: '0.1s' }}>.</span>
        <span className="animate-bounce" style={{ animationDelay: '0.2s' }}>.</span>
      </span>
    </div>
  );
}
