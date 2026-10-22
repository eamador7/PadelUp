# Padel Up Domain Model & Invariants

## Core Concepts
- **Dynamic Rating (PDR 1.00 – 7.00)**: Playtomic-style skill rating. Starts at 1.50–3.00 via onboarding quiz. Only confirmed non-offline matches update global rating.
- **Match Confirmation**: Matches require confirmation by at least 1 opponent (via QR scan or async notification) before rating points apply. Matches auto-confirm after 48 hours without disputes.
- **Americano Tournament**: Fixed-point round-robin (e.g. 32 points per match). Every round, pairs rotate so players partner with different teammates and play against varied opponents. Scores are conserved ($S_1 + S_2 = 32$). Individual players accumulate their team's points on a personal tournament leaderboard.
- **Mexicano Variant**: Dynamic ladder Americano where rounds after Round 1 are paired based on current tournament rank ($1+4$ vs $2+3$) with partner history checks to prevent duplicate partnerships.
- **Offline Tournament**: Standalone tournament managed entirely on a single phone with local guest player names. Does not require internet, accounts, or alter global player ratings.
- **Group Standing**: Internal leaderboard and rivalry matrix tracked exclusively within a group of friends.
