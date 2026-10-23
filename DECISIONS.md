# Design Decisions

Open questions left by the task description. Each one lists the options considered; the chosen option and a short rationale follow each one.

## Starting a match

### D1. What is the initial score of a new match?
- A. Always 0–0
- B. Caller may supply an initial score (e.g. joining a match already in progress)

Decision: A
Football default; the example applies scores via updates.

### D2. What happens when starting a match that is already in progress?
- A. Reject with an exception
- B. Ignore and return the existing match (idempotent)

Decision: A
Duplicate start is a caller bug and should not be hidden.

### D3. Can a team be in more than one match in progress at the same time?
- A. No, reject if either team is already playing
- B. Yes, only the exact same pairing is blocked

Decision: A
A team cannot physically play two matches at once.

### D4. How are team names validated?
- A. Reject null/blank names and a team playing itself
- B. A, plus normalise names (trim, case-insensitive comparison)
- C. No validation

Decision: B
Prevents "Spain" and "spain " being treated as different teams. Whitespace is normalised, not just trimmed: any run of whitespace, including Unicode and no-break spaces, becomes a single space and is stripped at the ends, so "Costa\u00A0Rica" and "Costa  Rica" are both "Costa Rica". Invisible formatting characters (zero-width spaces, soft hyphens, BOMs) are removed and names are put into Unicode NFC form, so differently encoded accents are the same team; names with control characters are rejected.

### D5. Can the same pairing be started again after it has finished?
- A. Yes
- B. No

Decision: A
Teams meet again in later rounds.

### D6. What does "start" return?
- A. Nothing (void)
- B. A generated match ID
- C. A match object / immutable snapshot

Decision: C
Immutable snapshot is useful to the caller at no cost.

## Updating the score

### D7. Absolute score or increment?
- A. Absolute: `updateScore(match, homeScore, awayScore)`
- B. Increment: goal for home/away
- C. Both

Decision: A
Matches "updated with these scores"; handles corrections naturally.

### D8. How are negative scores handled?
- A. Reject
- B. Allow

Decision: A
Negative scores are invalid in football.

### D9. Can a score decrease (e.g. VAR correction, data entry error)?
- A. Yes, any non-negative absolute score is accepted
- B. No, scores may only increase

Decision: A
VAR decisions and feed corrections happen.

### D10. What happens when updating an unknown or finished match?
- A. Throw an exception
- B. Silently ignore
- C. Return a boolean/result indicating failure

Decision: A
Fail fast; silently ignoring hides bugs.

### D11. How is a match identified in update/finish calls?
- A. By the home/away team pair
- B. By an ID returned from start
- C. By a handle/object returned from start

Decision: A
Simple API; unique thanks to D3.

## Finishing a match

### D12. What happens to a finished match?
- A. Removed from the scoreboard entirely
- B. Kept as history with a finished status

Decision: A
Live scoreboard only; history is out of scope.

### D13. What happens when finishing an unknown or already finished match?
- A. Throw an exception
- B. Silently ignore (idempotent)
- C. Return a boolean/result

Decision: A
Consistent with D10.

## Summary

### D14. What does the summary return?
- A. List of immutable snapshots (e.g. records)
- B. Formatted strings (e.g. "Uruguay 6 - Italy 6")
- C. Both (snapshots with a `toString`/formatter)

Decision: A
Typed data for callers; `toString` gives "Uruguay 6 - Italy 6" for readable tests.

### D15. How is "most recently started" determined?
- A. Wall-clock timestamp (`Instant`/`Clock`)
- B. Monotonic sequence number assigned at start
- C. Insertion order of the underlying collection

Decision: B
Deterministic; timestamps can collide and are hard to test.

### D16. Does a score update affect the tie-break order?
- A. No, only start order counts (as specified)
- B. Yes, most recently updated first

Decision: A
As specified: only start order breaks ties.

## Non-functional

### D17. Is the library thread-safe?
- A. Not thread-safe, documented as single-threaded
- B. Thread-safe via synchronisation of public methods
- C. Thread-safe via concurrent collections / finer-grained locking

Decision: B
Invariants span multiple steps (team check + sequence + insert), so a single lock is needed anyway; low contention. Upgrade path: read-write lock or volatile immutable snapshot if reads dominate.

### D18. How is storage handled?
- A. In-memory inside the scoreboard class
- B. In-memory behind a repository interface (swappable)

Decision: A
A repository interface would be premature for a simple library. The public API is a `Scoreboard` interface with a `public final InMemoryScoreboard` implementation that owns the storage.

### D19. Which Java version and test stack?
- A. Java 17 LTS
- B. Java 21 LTS
- Test stack: JUnit 5, with or without AssertJ

Decision: B
Java 21 LTS (records); JUnit 5 + AssertJ for clear list assertions.

### D20. How are errors reported?
- A. Unchecked exceptions (standard `IllegalArgumentException`/`IllegalStateException`)
- B. Custom unchecked exceptions (e.g. `MatchNotFoundException`)
- C. Checked exceptions
- D. Result objects

Decision: A + B
`IllegalArgumentException` for bad input; custom unchecked exceptions (`MatchNotFoundException`, `MatchAlreadyStartedException`, `TeamAlreadyPlayingException`) for state errors.

### D21. How far beyond "simple" should the scope go?
- A. Plain Java library, no frameworks, no persistence, no I/O
- B. Add extras (REST layer, persistence, CLI)

Decision: A
The task asks for a simple library.

## Additional operation

### D22. Which extra operation should be added (exactly one, in its own commit)?
- A. Get a single match by ID / teams
- B. List finished matches (requires D12-B)
- C. Score-change event listener
- D. Score correction / undo
- E. Find matches by team

Decision: E
`findMatchByTeam(team)` returning `Optional`; fits D3, realistic query, small and testable.

## Process

### D23. How detailed should AI.md be?
- A. Full verbatim prompt history plus summary
- B. Summarised prompt history plus key artifacts (e.g. this file)

Decision: A
The task explicitly asks for prompt history.

### D24. How should the commit history be structured?
- A. Small incremental commits (TDD-style: test + implementation per operation)
- B. A few larger commits per area, with the extra feature in its own commit

Decision: A
Easy to review; shows progression; extra feature gets its own commit.
