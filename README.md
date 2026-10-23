# Live Football World Cup Scoreboard

A small Java library that keeps track of football matches in progress and their scores.

- Java 21, no runtime dependencies
- Tests: JUnit 5 and AssertJ
- Build: Maven (wrapper included, no local Maven install needed)

## Build and test

```bash
./mvnw verify
```

## Usage

```java
Scoreboard scoreboard = new InMemoryScoreboard();

scoreboard.startMatch("Mexico", "Canada");
scoreboard.updateScore("Mexico", "Canada", 0, 5);
scoreboard.startMatch("Spain", "Brazil");
scoreboard.updateScore("Spain", "Brazil", 10, 2);
scoreboard.startMatch("Germany", "France");
scoreboard.updateScore("Germany", "France", 2, 2);
scoreboard.startMatch("Uruguay", "Italy");
scoreboard.updateScore("Uruguay", "Italy", 6, 6);
scoreboard.startMatch("Argentina", "Australia");
scoreboard.updateScore("Argentina", "Australia", 3, 1);

scoreboard.getSummary().forEach(System.out::println);
// Uruguay 6 - Italy 6
// Spain 10 - Brazil 2
// Mexico 0 - Canada 5
// Argentina 3 - Australia 1
// Germany 2 - France 2

scoreboard.finishMatch("Uruguay", "Italy");
```

## API

All types live in `com.sportradar.scoreboard`; exceptions in `com.sportradar.scoreboard.exception`.

| Operation | Behaviour | Errors |
|---|---|---|
| `Match startMatch(home, away)` | Adds a match at 0–0 and returns its snapshot | `IllegalArgumentException` (blank name, team playing itself), `MatchAlreadyStartedException`, `TeamAlreadyPlayingException` |
| `void updateScore(home, away, homeScore, awayScore)` | Sets the absolute score | `IllegalArgumentException` (blank name, negative score), `MatchNotFoundException` |
| `void finishMatch(home, away)` | Removes the match from the scoreboard | `IllegalArgumentException` (blank name), `MatchNotFoundException` |
| `List<Match> getSummary()` | Matches in progress, highest total score first; ties: most recently started first | none |
| `Optional<Match> findMatchByTeam(team)` | The match the team is playing in, home or away; empty if none ([extra feature](#extra-feature-find-a-match-by-team)) | `IllegalArgumentException` (blank name) |

`Match` is an immutable record (`homeTeam`, `awayTeam`, `homeScore`, `awayScore`) whose `toString()` is `Uruguay 6 - Italy 6`. Invalid input throws `IllegalArgumentException`; the three custom exceptions are unchecked and extend `IllegalStateException`.

## Extra feature: find a match by team

```java
// continuing the usage example above
scoreboard.findMatchByTeam("brazil");  // Optional[Spain 10 - Brazil 2]
scoreboard.findMatchByTeam("Italy");   // Optional.empty (match finished)
```

`findMatchByTeam` returns the match a team is currently playing in, whether at home or away, using the same name rules as the rest of the API (case and surrounding whitespace are ignored).

**Why this feature:**

- **It answers the most common question about a live scoreboard:** "how is my team doing?". Today a caller would have to fetch the whole summary and search it, and get the home/away and name-normalisation rules right themselves.
- **It removes the need to know home vs. away.** `updateScore` and `finishMatch` need the exact home/away pair; `findMatchByTeam` lets a caller look that up from a single team name.
- **It builds on an existing rule.** Because a team plays at most one match at a time, the result is a single optional match, not a list, so the API stays simple.
- **It is small, read-only and low risk.** It adds no state and cannot break the scoreboard's invariants.

Alternatives considered (a history of finished matches, a score-change listener, undo) were rejected because they either contradict a decision (finished matches are removed) or add far more state and complexity than a "simple library" needs. See D22 in [DECISIONS.md](DECISIONS.md).

## Assumptions

The task leaves several questions open on purpose. Each one, with the options considered and the reasoning, is recorded in [DECISIONS.md](DECISIONS.md). In short:

- **A new match starts at 0–0.** Scores come in through updates, as in the example.
- **A match is identified by its home/away pair.** Home and away are not interchangeable: Spain–Poland does not find Poland–Spain.
- **A team plays at most one match at a time.** Starting a match for a team that is already playing is rejected, as is starting the same pairing twice.
- **Team names are normalised.** Surrounding whitespace is stripped, any whitespace inside a name becomes a single space (including Unicode and no-break spaces), and comparison ignores case. So `" spain"` and `"SPAIN "` are the same team, and so are `"Costa\u00A0Rica"` and `"Costa  Rica"`. Invisible formatting characters (zero-width spaces, soft hyphens) are removed and names are put into Unicode NFC form, so an accent typed as one character or as a letter plus a combining mark is the same team. Names containing control characters are rejected. The spelling used when the match was started is the one shown.
- **Scores are absolute, not increments.** This matches "updated with these scores" and makes corrections natural.
- **Scores may go down** (VAR decisions, feed corrections), **but never below zero.**
- **Finished matches are removed.** This is a live scoreboard; history is out of scope. Both teams are free to play again, including a rematch.
- **Invalid operations fail fast** with an exception rather than being silently ignored.
- **"Most recently started" means start order**, not wall-clock time. Score updates do not change it; a rematch counts as newly started.

## Design and reasoning

- **`Scoreboard` interface plus a `public final InMemoryScoreboard`.** Callers depend on the interface; the implementation owns its storage. A repository layer would be premature for a library this size.
- **Immutable snapshots.** `startMatch` and `getSummary` return `Match` records and an unmodifiable list, so callers can never change the scoreboard's internal state, and a summary does not change after later updates.
- **Team-name handling in one place.** The package-private `TeamName` record validates and normalises names; nothing else does.
- **Structured keys.** Matches are keyed by a record holding both normalised names, never a joined string, so `"AB"`/`"CDE"` and `"ABC"`/`"DE"` can never collide.
- **Start sequence instead of timestamps.** Each start takes the next value of a counter. Timestamps can be equal for two quick starts and make tests depend on a clock; a counter is deterministic.
- **Sort on read.** `getSummary` sorts the matches in progress on each call. With the handful of matches a World Cup has at once, this is cheaper and simpler than keeping a sorted structure up to date on every update.

## Trade-offs

- **Fail-fast exceptions vs. lenient API.** Rejecting unknown or duplicate operations surfaces caller bugs early, at the cost of callers having to handle exceptions.
- **Team pair as identifier vs. match ID.** The API is simple and needs no ID bookkeeping, but it relies on the "one match per team" rule and requires callers to pass home and away in the right order.
- **Case-insensitive names.** Names are lower-cased with `Locale.ROOT`. This covers the usual cases but is not full Unicode case folding (e.g. `"ß"` and `"SS"` are different teams).
- **Removing invisible formatting characters.** This stops invisible duplicates such as `"Spain\u200B"`, but the same character class is used by some emoji: a name containing an emoji built with zero-width joiners, or a subdivision flag such as England's, loses those joining characters.
- **No history.** Removing finished matches keeps the model small, but nothing can be queried about a match once it is finished.

## Thread safety

`InMemoryScoreboard` is thread-safe: every public method is `synchronized`. Starting a match checks several things before inserting (pairing, both teams, start sequence), and these checks must be atomic together. Concurrent collections alone would not achieve that, so a lock is needed anyway. With a few matches and rare goals, contention on a single lock is negligible.

Concurrency tests run many threads starting, updating and finishing matches at the same time. They are smoke tests: they reliably caught the races before synchronisation was added, but they cannot prove the absence of races.

Known trade-offs and how to change them without touching the API:

- **Reads block writes and each other.** `getSummary` currently sorts while holding the lock. If the summary were read very often (many clients polling), there is an upgrade path in increasing order of effort, each step only if measurements show the previous one is not enough:
  1. **Copy inside the lock, sort outside it.** Take a snapshot of the matches in progress under the lock, release it, then sort the copy. This is safe because the stored values are immutable records, and the copy is still taken atomically, so it reflects one consistent state. It shortens the time the lock is held to a plain copy.
  2. **Read-write lock.** Replace `synchronized` with a `ReentrantReadWriteLock`, so many readers can take the summary at the same time while writers still get exclusive access.
  3. **Lock-free reads.** Writers build an immutable, pre-sorted summary after each change and publish it through a `volatile` field; `getSummary` just returns it and never takes a lock. Writes get slightly more expensive, which suits a scoreboard that is read far more often than it changes.
- **The lock is the instance itself.** Code outside the library that synchronises on the same `InMemoryScoreboard` object could block it. A private lock object would prevent this; synchronised methods were kept for simplicity, as decided in [DECISIONS.md](DECISIONS.md) (D17).

## Project documents

- [DECISIONS.md](DECISIONS.md): open questions from the task, options considered, decisions and reasoning
- [PLAN.md](PLAN.md): the step-by-step, test-first implementation plan
- [CLAUDE.md](CLAUDE.md): project rules followed by the AI assistant
