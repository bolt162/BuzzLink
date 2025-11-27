'use client';

import { UserButton, useUser } from '@clerk/nextjs';
import Link from 'next/link';

export default function Header() {
  const { user } = useUser();

  return (
    <header className="bg-primary text-white px-6 py-3 flex items-center justify-between shadow-md">
      <div className="flex items-center space-x-4">
        <h1 className="text-2xl font-bold">BuzzLink</h1>
        {user?.publicMetadata?.isAdmin && (
          <span className="bg-yellow-500 text-black text-xs font-semibold px-2 py-1 rounded">
            ADMIN
          </span>
        )}
      </div>
      <div className="flex items-center space-x-4">
        <Link href="/profile" className="hover:underline">
          Profile
        </Link>
        <UserButton afterSignOutUrl="/" />
      </div>
    </header>
  );
}
