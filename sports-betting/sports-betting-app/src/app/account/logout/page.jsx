'use client';
import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';

function MainComponent() {
  const { logout } = useAuth0();

  const handleSignOut = () => {
    logout({
      returnTo: typeof window !== 'undefined' ? window.location.origin : '/',
    });
  };

  return (
    <div className="flex min-h-screen w-full items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-50 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl">
        <h1 className="mb-8 text-center text-3xl font-bold text-gray-800">
          Sign Out
        </h1>

        <button
          onClick={handleSignOut}
          className="w-full rounded-lg bg-[#357AFF] px-4 py-3 text-base font-medium text-white transition-colors hover:bg-[#2E69DE] focus:outline-none focus:ring-2 focus:ring-[#357AFF] focus:ring-offset-2 disabled:opacity-50"
        >
          Sign Out
        </button>
      </div>
    </div>
  );
}

export default MainComponent;
