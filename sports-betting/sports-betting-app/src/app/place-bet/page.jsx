'use client';
import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

function MainComponent() {
  const { user, isLoading: userLoading, isAuthenticated } = useAuth0();
  const [betAmount, setBetAmount] = useState('');
  const [selectedTeam, setSelectedTeam] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [balance, setBalance] = useState(null);
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [balanceError, setBalanceError] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  const [selectedEventId, setSelectedEventId] = useState(null);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      const id = parseInt(params.get('eventId'), 10);
      if (!isNaN(id)) {
        setSelectedEventId(id);
      }
    }
  }, []);

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

  // SSE: Subscribe to matches stream
    useEffect(() => {
      const eventSource = new EventSource("http://localhost:7114/matches");

      eventSource.onmessage = (event) => {
        try {
          const newEvent = JSON.parse(event.data);
          setUpcomingEvents((prev) => {
            const index = prev.findIndex((e) => e.id === newEvent.id);
            if (index !== -1) {
              const updated = [...prev];
              updated[index] = newEvent;
              return updated;
            } else {
              // Add new event
              return [...prev, newEvent];
            }
          });
        } catch (err) {
          console.error("Invalid event format", err);
        }
      };

      eventSource.onerror = (err) => {
        console.error("SSE connection error:", err);
        eventSource.close();
      };

      return () => {
        eventSource.close();
      };
    }, []);

  const selectedEvent = selectedEventId !== null
    ? upcomingEvents.find((e) => e.id === selectedEventId)
    : null;

  const calculatePotentialWinnings = () => {
    if (!betAmount || !selectedTeam || !selectedEvent) return 0;
    const odds =
      selectedTeam === 'home'
        ? selectedEvent.odds.home
        : selectedEvent.odds.away;
    const numericOdds = parseInt(odds);
    if (numericOdds > 0) {
      return ((betAmount * numericOdds) / 100).toFixed(2);
    } else {
      return (betAmount * (100 / Math.abs(numericOdds))).toFixed(2);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    if (!selectedEvent || !selectedTeam || !betAmount || betAmount <= 0 || betAmount > balance) {
      setError('Please select an event, team, and enter valid bet amount');
      setLoading(false);
      return;
    }

    try {
      const userId = user.sub.replace('google-oauth2|', '');

      const payload = {
        userId,
        eventId: selectedEvent.id,
        team: selectedTeam,
        amount: parseFloat(betAmount),
      };

      const response = await fetch('http://localhost:7114/bet', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data?.message || 'Failed to place bet');
        setErrorMessage('Oh no! Looks like your bet failed');
        setTimeout(() => {
          setErrorMessage('');
          window.location.href = '/';
        }, 2000);
      }
      setSuccessMessage('✅ Bet placed successfully. Once verified it will appear in My Bets section');
      setTimeout(() => {
        setSuccessMessage('');
        window.location.href = '/bet-history';
      }, 2000);
    } catch (err) {
      console.error(err);
      setError(err.message || 'Failed to place bet. Please try again.');
      setLoading(false);
    }
  };


  if (userLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-100">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-100">
        <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
          <h2 className="mb-4 text-center text-2xl font-bold text-gray-800">
            Sign In Required
          </h2>
          <p className="mb-6 text-center text-gray-600">
            Please sign in to place bets
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

          <a href="/bet-history" className="rounded-lg border border-white px-4 py-2 hover:bg-white hover:text-[#357AFF] transition-colors">
            My Bets
          </a>

          <a
            href="/account/logout"
            className="rounded-lg border border-white px-4 py-2 hover:bg-white hover:text-[#357AFF] transition-colors"
          >
            Sign Out
          </a>
        </div>
      </div>
    </header>

      <main className="mx-auto max-w-3xl p-4">
        <div className="rounded-lg bg-white p-6 shadow-md space-y-6">

          {selectedEvent && (
            <>
              <div className="border-b pb-4">
                <div className="mb-2 flex items-center justify-between">
                  <span className="text-sm text-gray-500">
                    {new Date(selectedEvent.time).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                      hour: 'numeric',
                      minute: '2-digit',
                    })}
                  </span>
                  <span className="rounded-full bg-gray-100 px-3 py-1 text-sm capitalize">
                    {selectedEvent.sport}
                  </span>
                </div>
                <h2 className="text-xl font-bold text-gray-800">
                  {selectedEvent.teams[0]} vs {selectedEvent.teams[1]}
                </h2>
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="space-y-4">
                  {/* Team Selector */}
                  <div className="flex flex-col space-y-2">
                    <label className="text-sm font-medium text-gray-700">
                      Select Team
                    </label>
                    <div className="grid grid-cols-2 gap-4">
                      <button
                        type="button"
                        onClick={() => setSelectedTeam('home')}
                        className={`rounded-lg border p-4 text-left transition-colors ${
                          selectedTeam === 'home'
                            ? 'border-[#357AFF] bg-blue-50'
                            : 'border-gray-200 hover:border-[#357AFF]'
                        }`}
                      >
                        <div className="font-medium">
                          {selectedEvent.teams[0]}
                        </div>
                        <div className="text-[#357AFF]">
                          {selectedEvent.odds.home}
                        </div>
                      </button>
                      <button
                        type="button"
                        onClick={() => setSelectedTeam('away')}
                        className={`rounded-lg border p-4 text-left transition-colors ${
                          selectedTeam === 'away'
                            ? 'border-[#357AFF] bg-blue-50'
                            : 'border-gray-200 hover:border-[#357AFF]'
                        }`}
                      >
                        <div className="font-medium">
                          {selectedEvent.teams[1]}
                        </div>
                        <div className="text-[#357AFF]">
                          {selectedEvent.odds.away}
                        </div>
                      </button>
                    </div>
                  </div>

                  {/* Bet Amount */}
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">
                      Bet Amount
                    </label>
                    <div className="overflow-hidden rounded-lg border border-gray-200 bg-white px-4 py-3">
                      <input
                        type="number"
                        name="betAmount"
                        value={betAmount}
                        onChange={(e) => setBetAmount(e.target.value)}
                        placeholder="Enter amount"
                        className="w-full bg-transparent text-lg outline-none"
                      />
                    </div>
                  </div>

                  {/* Potential Winnings */}
                  <div className="rounded-lg bg-gray-50 p-4">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">
                        Potential Winnings:
                      </span>
                      <span className="font-medium text-green-600">
                        ${calculatePotentialWinnings()}
                      </span>
                    </div>
                  </div>
                </div>

                {error && (
                  <div className="rounded-lg bg-red-50 p-3 text-sm text-red-500">
                    {error}
                  </div>
                )}
                  {successMessage && (
                    <div className="rounded-lg bg-green-100 border border-green-300 p-4 text-green-700">
                      {successMessage}
                    </div>
                  )}

                <button
                  type="submit"
                  disabled={loading || !selectedTeam || !betAmount}
                  className="w-full rounded-lg bg-[#357AFF] px-4 py-3 text-base font-medium text-white transition-colors hover:bg-[#2E69DE] focus:outline-none focus:ring-2 focus:ring-[#357AFF] focus:ring-offset-2 disabled:opacity-50"
                >
                  {loading ? 'Processing...' : 'Place Bet'}
                </button>
              </form>
            </>
          )}
        </div>
      </main>
    </div>
  );
}

export default MainComponent;
