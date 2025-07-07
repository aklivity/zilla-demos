// src/app/layout.js

import { Inter } from 'next/font/google';
import './globals.css';
import Auth0ProviderWrapper from './auth-provider-wrapper';

const inter = Inter({ subsets: ['latin'] });

export const metadata = {
  title: 'Sports Betting',
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <Auth0ProviderWrapper>{children}</Auth0ProviderWrapper>
      </body>
    </html>
  );
}
