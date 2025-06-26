'use client';
import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

function MainComponent() {
  const { user, isLoading: userLoading, isAuthenticated } = useAuth0();
  const [filter, setFilter] = useState('all');
  const [bets, setBets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [balance, setBalance] = useState(null);
  const [balanceError, setBalanceError] = useState(null);

  // Fetch balance
    useEffect(() => {
      if (user && isAuthenticated) {
        const userId = user.sub.replace('google-oauth2|', '');
        const eventSource = new EventSource(`http://localhost:7114/user-profile/${userId}`);

        eventSource.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);

            if (data.balance !== undefined) {
              setBalance((prevBalance) => {
                if (prevBalance !== data.balance) {
                  return data.balance;
                }
                return prevBalance;
              });
              setBalanceError(null);
            }
          } catch (err) {
            console.error('Failed to parse SSE data:', err);
            setBalanceError('Invalid data received');
          }
        };

        eventSource.onerror = (err) => {
          console.error('SSE connection error:', err);
          setBalanceError('Failed to connect to balance stream');
          eventSource.close();
        };

        return () => {
          eventSource.close();
        };
      }
    }, [user, isAuthenticated]);

  useEffect(() => {
    if (!user || !user.sub) return;

    const userId = user.sub.replace('google-oauth2|', '');
    const eventSource = new EventSource(`http://localhost:7114/bet-verified/${userId}`);

    let hasReceivedData = false;

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);

        const bet = {
          id: data.id,
          event_id: data.event_id,
          event_name: data.event_name,
          bet_type: data.bet_type,
          amount: data.amount,
          odds: data.odds,
          potential_winnings: data.potential_winnings,
          status: data.status?.toLowerCase(),
          result: data.result,
          created_at: data.created_at,
          settled_at: data.settled_at,
        };

        setBets((prevBets) => {
          const existingIndex = prevBets.findIndex((b) => b.id === bet.id);
          if (existingIndex !== -1) {
            const updatedBets = [...prevBets];
            updatedBets[existingIndex] = bet;
            return updatedBets;
          } else {
            return [...prevBets, bet];
          }
        });

        hasReceivedData = true;
        setLoading(false);
      } catch (err) {
        console.error('Error parsing SSE data', err);
        setError('Failed to load betting history');
        setLoading(false);
      }
    };

    eventSource.onerror = (err) => {
      console.error('EventSource failed:', err);
      setError('Failed to load betting history');
      setLoading(false);
      eventSource.close();
    };

    // fallback timeout in case no events are sent
    const timeout = setTimeout(() => {
      if (!hasReceivedData) {
        setLoading(false);
      }
    }, 100);

    return () => {
      eventSource.close();
      clearTimeout(timeout);
    };
  }, [user]);


  if (userLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-100">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-100">
        <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
          <h2 className="mb-4 text-center text-2xl font-bold text-gray-800">
            Sign In Required
          </h2>
          <p className="mb-6 text-center text-gray-600">
            Please sign in to view your betting history
          </p>
          <div className="flex justify-center space-x-4">
            <a
              href="/account/signin"
              className="rounded-lg bg-[#357AFF] px-6 py-2 text-white hover:bg-[#2E69DE]"
            >
              Sign In
            </a>
            <a
              href="/account/signup"
              className="rounded-lg border border-[#357AFF] px-6 py-2 text-[#357AFF] hover:bg-gray-50"
            >
              Sign Up
            </a>
          </div>
        </div>
      </div>
    );
  }

  const filteredBets = bets.filter((bet) => {
    if (filter === 'all') return true;
    return bet.status?.toLowerCase() === filter.toLowerCase();
  });

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-[#357AFF] text-white shadow-lg">
        <div className="mx-auto flex max-w-7xl items-center justify-between p-4">
          <a href="/" className="flex items-center space-x-4">
            <i className="fas fa-dice text-2xl"></i>
            <h1 className="text-xl font-bold">BetSports</h1>
          </a>

          <div className="flex items-center space-x-6">
            <div className="text-white mr-4">
              👋 Welcome, <span className="font-semibold">{user.name || user.email}</span>
            </div>
            <div className="rounded-lg bg-[#2E69DE] px-4 py-2">
              <span className="mr-2">Balance:</span>
              <span className="font-bold">
                ${balance !== null ? balance.toFixed(2) : "..."}
              </span>
            </div>

            <a
              href="/account/logout"
              className="rounded-lg border border-white px-4 py-2 hover:bg-white hover:text-[#357AFF] transition-colors"
            >
              Sign Out
            </a>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl p-4">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-gray-800">Betting History</h2>
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="rounded-lg border border-gray-200 bg-white px-4 py-2 text-gray-700 focus:border-[#357AFF] focus:outline-none focus:ring-1 focus:ring-[#357AFF]"
          >
            <option value="all">All Bets</option>
            <option value="won">Won</option>
            <option value="lost">Lost</option>
            <option value="pending">Pending</option>
          </select>
        </div>

        {error && (
          <div className="mb-4 rounded-lg bg-red-50 p-4 text-red-500">
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center text-gray-600">Loading bets...</div>
        ) : filteredBets.length === 0 ? (
          <div className="rounded-lg bg-white p-8 text-center shadow-md">
            <div className="mb-4 text-4xl">🎲</div>
            <h3 className="mb-2 text-xl font-medium text-gray-800">
              No bets found
            </h3>
            <p className="text-gray-600">
              {filter === 'all'
                ? "You haven't placed any bets yet"
                : `No ${filter} bets found`}
            </p>
            <a
              href="/"
              className="mt-4 inline-block rounded-lg bg-[#357AFF] px-6 py-2 text-white hover:bg-[#2E69DE]"
            >
              Place a Bet
            </a>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredBets.map((bet) => (
              <div
                key={bet.id}
                className="rounded-lg bg-white p-6 shadow-md transition-shadow hover:shadow-lg"
              >
                <div className="mb-4 flex items-center justify-between">
                  <span className="text-sm text-gray-500">
                    {new Date(bet.created_at).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                      year: 'numeric',
                    })}
                  </span>
                  <span
                    className={`rounded-full px-3 py-1 text-sm font-medium ${
                      bet.status === 'won'
                        ? 'bg-green-100 text-green-800'
                        : bet.status === 'lost'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {bet.status.charAt(0).toUpperCase() + bet.status.slice(1)}
                  </span>
                </div>
                <h3 className="mb-2 text-lg font-medium text-gray-800">
                  {bet.event_name}
                </h3>
                <div className="grid grid-cols-3 gap-4 text-sm">
                  <div>
                    <div className="text-gray-500">Bet Amount</div>
                    <div className="font-medium">${bet.amount.toFixed(2)}</div>
                  </div>
                  <div>
                    <div className="text-gray-500">Odds</div>
                    <div className="font-medium">{bet.odds.toFixed(2)}</div>
                  </div>
                  <div>
                    <div className="text-gray-500">Potential Win</div>
                    <div className="font-medium">
                      ${bet.potential_winnings.toFixed(2)}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

export default MainComponent;
