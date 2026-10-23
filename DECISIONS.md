# Design Decisions

Open questions left by the task description. Each one lists the options considered; decisions are filled in as they are made.

## Starting a match

### D1. What is the initial score of a new match?
- A. Always 0–0
- B. Caller may supply an initial score (e.g. joining a match already in progress)

Decision: TBD

### D2. What happens when starting a match that is already in progress?
- A. Reject with an exception
- B. Ignore and return the existing match (idempotent)

Decision: TBD

### D3. Can a team be in more than one match in progress at the same time?
- A. No, reject if either team is already playing
- B. Yes, only the exact same pairing is blocked

Decision: TBD

### D4. How are team names validated?
- A. Reject null/blank names and a team playing itself
- B. A, plus normalise names (trim, case-insensitive comparison)
- C. No validation

Decision: TBD

### D5. Can the same pairing be started again after it has finished?
- A. Yes
- B. No

Decision: TBD

### D6. What does "start" return?
- A. Nothing (void)
- B. A generated match ID
- C. A match object / immutable snapshot

Decision: TBD

## Updating the score

### D7. Absolute score or increment?
- A. Absolute: `updateScore(match, homeScore, awayScore)`
- B. Increment: goal for home/away
- C. Both

Decision: TBD

### D8. How are negative scores handled?
- A. Reject
- B. Allow

Decision: TBD

### D9. Can a score decrease (e.g. VAR correction, data entry error)?
- A. Yes, any non-negative absolute score is accepted
- B. No, scores may only increase

Decision: TBD

### D10. What happens when updating an unknown or finished match?
- A. Throw an exception
- B. Silently ignore
- C. Return a boolean/result indicating failure

Decision: TBD

### D11. How is a match identified in update/finish calls?
- A. By the home/away team pair
- B. By an ID returned from start
- C. By a handle/object returned from start

Decision: TBD

## Finishing a match

### D12. What happens to a finished match?
- A. Removed from the scoreboard entirely
- B. Kept as history with a finished status

Decision: TBD

### D13. What happens when finishing an unknown or already finished match?
- A. Throw an exception
- B. Silently ignore (idempotent)
- C. Return a boolean/result

Decision: TBD

## Summary

### D14. What does the summary return?
- A. List of immutable snapshots (e.g. records)
- B. Formatted strings (e.g. "Uruguay 6 - Italy 6")
- C. Both (snapshots with a `toString`/formatter)

Decision: TBD

### D15. How is "most recently started" determined?
- A. Wall-clock timestamp (`Instant`/`Clock`)
- B. Monotonic sequence number assigned at start
- C. Insertion order of the underlying collection

Decision: TBD

### D16. Does a score update affect the tie-break order?
- A. No, only start order counts (as specified)
- B. Yes, most recently updated first

Decision: TBD

## Non-functional

### D17. Is the library thread-safe?
- A. Not thread-safe, documented as single-threaded
- B. Thread-safe via synchronisation of public methods
- C. Thread-safe via concurrent collections / finer-grained locking

Decision: TBD

### D18. How is storage handled?
- A. In-memory inside the scoreboard class
- B. In-memory behind a repository interface (swappable)

Decision: TBD

### D19. Which Java version and test stack?
- A. Java 17 LTS
- B. Java 21 LTS
- Test stack: JUnit 5, with or without AssertJ

Decision: TBD

### D20. How are errors reported?
- A. Unchecked exceptions (standard `IllegalArgumentException`/`IllegalStateException`)
- B. Custom unchecked exceptions (e.g. `MatchNotFoundException`)
- C. Checked exceptions
- D. Result objects

Decision: TBD

### D21. How far beyond "simple" should the scope go?
- A. Plain Java library, no frameworks, no persistence, no I/O
- B. Add extras (REST layer, persistence, CLI)

Decision: TBD

## Additional operation

### D22. Which extra operation should be added (exactly one, in its own commit)?
- A. Get a single match by ID / teams
- B. List finished matches (requires D12-B)
- C. Score-change event listener
- D. Score correction / undo
- E. Find matches by team

Decision: TBD

## Process

### D23. How detailed should AI.md be?
- A. Full verbatim prompt history plus summary
- B. Summarised prompt history plus key artifacts (e.g. this file)

Decision: TBD

### D24. How should the commit history be structured?
- A. Small incremental commits (TDD-style: test + implementation per operation)
- B. A few larger commits per area, with the extra feature in its own commit

Decision: TBD
