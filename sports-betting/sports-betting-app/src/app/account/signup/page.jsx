'use client';
import { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

export default function SignupPage() {
  const { loginWithRedirect, isAuthenticated } = useAuth0();

  useEffect(() => {
    if (!isAuthenticated) {
      loginWithRedirect({
        screen_hint: 'signup',
      });
    }
  }, [isAuthenticated, loginWithRedirect]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <p className="text-gray-600">Redirecting to sign-up...</p>
    </div>
  );
}
