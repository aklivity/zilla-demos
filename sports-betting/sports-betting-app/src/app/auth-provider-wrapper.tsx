'use client';
import { Auth0Provider } from '@auth0/auth0-react';

export default function Auth0ProviderWrapper({ children }) {
  return (
    <Auth0Provider
      domain="aklivity.us.auth0.com"
      clientId="FoHwttWzOuNqI1TBYWjDk6J0K3m9z3Vh"
      authorizationParams={{
        redirect_uri: typeof window !== 'undefined' ? window.location.origin : 'http://localhost:3000',
      }}
      useRefreshTokens={true}
      cacheLocation="localstorage"
    >
      {children}
    </Auth0Provider>
  );
}
