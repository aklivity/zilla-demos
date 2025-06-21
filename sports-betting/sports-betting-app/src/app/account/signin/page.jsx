'use client';
import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

function MainComponent() {
  const { loginWithRedirect, isLoading, error } = useAuth0();

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      await loginWithRedirect({
        redirectUri: window.location.origin,
      });
    } catch (err) {
      console.error('Auth0 login error:', err);
    }
  };

  return (
    <div className="flex min-h-screen w-full items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-50 p-4">
      <form
        onSubmit={handleLogin}
        className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl"
      >
        <h1 className="mb-8 text-center text-3xl font-bold text-gray-800">
          Welcome Back
        </h1>

        {error && (
          <div className="rounded-lg bg-red-50 p-3 text-sm text-red-500">
            {error.message}
          </div>
        )}

        <button
          type="submit"
          disabled={isLoading}
          className="w-full rounded-lg bg-[#357AFF] px-4 py-3 text-base font-medium text-white transition-colors hover:bg-[#2E69DE] focus:outline-none focus:ring-2 focus:ring-[#357AFF] focus:ring-offset-2 disabled:opacity-50"
        >
          {isLoading ? 'Loading...' : 'Sign In with Auth0'}
        </button>

        <p className="text-center text-sm text-gray-600 mt-4">
          Don&apos;t have an account?{" "}
          <a
            href="/account/signup"
            className="text-[#357AFF] hover:text-[#2E69DE]"
          >
            Sign up
          </a>
        </p>
      </form>
    </div>
  );
}

export default MainComponent;
