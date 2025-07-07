"use client";
import React, { useState, useEffect } from "react";
import { useAuth0 } from "@auth0/auth0-react";

function MainComponent() {
  const { user, isLoading: userLoading, isAuthenticated } = useAuth0();
  const [selectedSport, setSelectedSport] = useState("all");
  const [balance, setBalance] = useState(null);
  const [balanceError, setBalanceError] = useState(null);
  const [upcomingEvents, setUpcomingEvents] = useState([]);

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
        setBalanceError('...');
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

          if (newEvent.status === 'COMPLETED') {
            // Remove the event if it exists
            if (index !== -1) {
              const updated = [...prev];
              updated.splice(index, 1);
              return updated;
            }
            return prev; // It doesn't exist, no change
          }

          // Otherwise, update or add the event
          if (index !== -1) {
            const updated = [...prev];
            updated[index] = newEvent;
            return updated;
          } else {
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

  const handleGiveMoneyClick = async () => {
    if (!user) return;
    const userId = user.sub.replace('google-oauth2|', '');
    try {
      const response = await fetch("http://localhost:7114/user", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Idempotency-Key": userId,
        },
        body: JSON.stringify({
          userId: userId,
          balance: 100000,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error ${response.status}`);
      }

      const data = await response.json();
      console.log("Balance update success:", data);
    } catch (err) {
      console.error("Failed to update balance:", err);
    }
  };

  const sportsCategories = [
    { id: "football", name: "Football", icon: "fa-football" },
    { id: "basketball", name: "Basketball", icon: "fa-basketball" },
    { id: "baseball", name: "Baseball", icon: "fa-baseball" },
    { id: "hockey", name: "Hockey", icon: "fa-hockey-puck" },
    { id: "soccer", name: "Soccer", icon: "fa-futbol" },
  ];

  const filteredEvents =
    selectedSport === "all"
      ? upcomingEvents
      : upcomingEvents.filter((event) => event.sport === selectedSport);

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-[#357AFF] text-white shadow-lg">
        <div className="mx-auto flex max-w-7xl items-center justify-between p-4">
          <a href="/" className="flex items-center space-x-4">
            <i className="fas fa-dice text-2xl"></i>
            <h1 className="text-xl font-bold">BetSports</h1>
          </a>

          <nav className="flex items-center space-x-6">
            {user ? (
              <>
               <div className="text-white mr-4">
                  👋 Welcome, <span className="font-semibold">{user.name || user.email}</span>
               </div>
               <div className="rounded-lg bg-[#2E69DE] px-4 py-2">
                                <span className="mr-2">Balance:</span>
                                {balanceError ? (
                                  <span className="text-red-200">{balanceError}</span>
                                ) : (
                                  <span className="font-bold">
                                    ${balance !== null ? balance.toFixed(2) : "..."}
                                  </span>
                                )}
                </div>
                <button
                  onClick={handleGiveMoneyClick}
                  className="ml-4 rounded-lg bg-green-500 px-4 py-2 text-white hover:bg-green-600"
                >
                  $$$
                </button>
                <a href="/bet-history" className="rounded-lg border border-white px-4 py-2 hover:bg-white hover:text-[#357AFF] transition-colors">
                  My Bets
                </a>
                <a href="/account/logout" className="rounded-lg border border-white px-4 py-2 hover:bg-white hover:text-[#357AFF] transition-colors">
                  Sign Out
                </a>
              </>
            ) : (
              <>
                <a href="/account/signin" className="hover:text-gray-200">
                  Sign In
                </a>
                <a
                  href="/account/signup"
                  className="rounded-lg bg-white px-4 py-2 text-[#357AFF] hover:bg-gray-100"
                >
                  Sign Up
                </a>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-7xl p-4">
        {!user && !userLoading && (
          <div className="mb-8 rounded-lg bg-white p-6 shadow-md">
            <h2 className="mb-4 text-2xl font-bold text-gray-800">
              Welcome to BetSports
            </h2>
            <p className="mb-4 text-gray-600">
              Join now to place bets on your favorite sports events!
            </p>
            <div className="flex space-x-4">
              <a
                href="/account/signup"
                className="rounded-lg bg-[#357AFF] px-6 py-2 text-white hover:bg-[#2E69DE]"
              >
                Create Account
              </a>
              <a
                href="/account/signin"
                className="rounded-lg border border-[#357AFF] px-6 py-2 text-[#357AFF] hover:bg-gray-50"
              >
                Sign In
              </a>
            </div>
          </div>
        )}

        <div className="mb-8 grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-6">
          <button
            onClick={() => setSelectedSport("all")}
            className={`rounded-lg p-4 text-center shadow-md transition-colors ${
              selectedSport === "all"
                ? "bg-[#357AFF] text-white"
                : "bg-white text-gray-800 hover:bg-gray-50"
            }`}
          >
            <i className="fas fa-globe mb-2 text-2xl"></i>
            <div className="text-sm font-medium">All</div>
          </button>
          {sportsCategories.map((sport) => (
            <button
              key={sport.id}
              onClick={() => setSelectedSport(sport.id)}
              className={`rounded-lg p-4 text-center shadow-md transition-colors ${
                selectedSport === sport.id
                  ? "bg-[#357AFF] text-white"
                  : "bg-white text-gray-800 hover:bg-gray-50"
              }`}
            >
              <i className={`fas ${sport.icon} mb-2 text-2xl`}></i>
              <div className="text-sm font-medium">{sport.name}</div>
            </button>
          ))}
        </div>

        {/* Featured Events */}
        <div className="space-y-4">
          <h2 className="text-2xl font-bold text-gray-800">Featured Events</h2>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {filteredEvents.map((event) => (
              <div
                key={`${event.id}-${event.teams.join("")}`}
                className="rounded-lg bg-white p-6 shadow-md"
              >
                <div className="mb-4 flex items-center justify-between">
                  <span className="text-sm text-gray-500">
                    {new Date(event.time).toLocaleDateString("en-US", {
                      month: "short",
                      day: "numeric",
                      hour: "numeric",
                      minute: "2-digit",
                    })}
                  </span>
                  <span className="rounded-full bg-gray-100 px-3 py-1 text-sm capitalize">
                    {event.sport}
                  </span>
                </div>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="font-medium">{event.teams[0]}</span>
                    <span className="text-[#357AFF] font-medium">
                      {event.odds.home}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="font-medium">{event.teams[1]}</span>
                    <span className="text-[#357AFF] font-medium">
                      {event.odds.away}
                    </span>
                  </div>
                  {user && (
                    <div className="flex justify-center">
                        <a
                          href={`/place-bet?eventId=${event.id}`}
                          className="mt-4 inline-block rounded-lg bg-[#21CBAD] px-6 py-2 text-white hover:bg-[#2E69DE]">
                          Place Bet
                        </a>
                    </div>
                  )}
                </div>
                {!user && (
                  <div className="mt-4 text-center text-sm text-gray-500">
                    Sign in to place bets
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}

export default MainComponent;
