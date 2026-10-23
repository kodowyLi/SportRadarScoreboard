# Implementation Plan

Based on the task description and [DECISIONS.md](DECISIONS.md).

Package: `com.sportradar.scoreboard`

Workflow for every step: write tests → run `mvn test` and see them fail for the right reason → implement → `mvn verify` green → review diff → commit (only after approval). One commit per step.

## Step 1: Maven skeleton
- [x] `pom.xml`: Java 21 (`maven.compiler.release`), UTF-8
- [x] JUnit Jupiter 5 and AssertJ (test scope)
- [x] Pinned `maven-compiler-plugin` and `maven-surefire-plugin` versions
- [x] Maven wrapper (`mvnw`, Maven 3.9.9) so the build doesn't depend on a local Maven install
- [x] `./mvnw verify` builds with no sources
- [x] Commit: *Add Maven project skeleton*

## Step 2: Start a match (D1–D6, D11, D14, D20)
Tests first:
- [x] Starting a match returns a `Match` snapshot at 0–0
- [x] `getSummary()` includes the new match
- [x] Null or blank team name → `IllegalArgumentException`
- [x] A team playing itself → `IllegalArgumentException`
- [x] Names are stripped of surrounding (including Unicode) whitespace and compared ignoring case
- [x] Whitespace inside a name collapses to a single space: "Costa\u00A0Rica" and "Costa  Rica" are the same team, shown as "Costa Rica"
- [x] Invisible formatting characters are removed ("Spain\u200B" is Spain) and names are NFC-normalised (composed and decomposed "Curacao" cedilla are the same team)
- [x] Names with control characters, or with no visible characters, → `IllegalArgumentException`
- [x] Starting the same pairing twice → `MatchAlreadyStartedException`
- [x] A team that is already playing → `TeamAlreadyPlayingException` (e.g. Poland–Brazil is live, so Spain–Poland is rejected)
- [x] Match keys don't collide: "AB" vs "CDE" and "ABC" vs "DE" are separate matches and both can start
- [x] Reversed pairing while in progress (Brazil–Spain while Spain–Brazil is live) → `TeamAlreadyPlayingException`
- [x] A rejected start leaves the scoreboard unchanged
- [x] `Match.toString` → "Uruguay 6 - Italy 6" (`MatchTest`)
- [x] `TeamNameTest`: Unicode whitespace, no-break spaces (U+00A0, U+2007, U+202F), ignore case, null/blank

Implementation:
- [x] `Match` record (home, away, homeScore, awayScore), `toString` → "Uruguay 6 - Italy 6"
- [x] `Scoreboard` interface and `public final InMemoryScoreboard`: `startMatch`, basic `getSummary`
- [x] `MatchAlreadyStartedException`, `TeamAlreadyPlayingException` in `com.sportradar.scoreboard.exception`
- [x] Package-private `TeamName` record: the single place for validation and normalisation (collapses Unicode whitespace incl. no-break spaces to single spaces and strips the ends, `Locale.ROOT` lower case)
- [x] Add `TeamAlreadyPlayingException` to D20 in DECISIONS.md
- [x] Commit: *Add starting a match*

## Step 3: Update the score (D7–D10)
Tests first:
- [x] An absolute update is reflected in the summary
- [x] A lower score is allowed
- [x] Negative score → `IllegalArgumentException`
- [x] Unknown match → `MatchNotFoundException`
- [x] Home and away are kept apart: if Poland–Spain is live, `updateScore("Spain", "Poland", …)` → `MatchNotFoundException`, and the live match is unchanged

Implementation:
- [x] `updateScore(home, away, homeScore, awayScore)`
- [x] `MatchNotFoundException`
- [x] Commit: *Add updating the score*

## Step 4: Finish a match (D5, D12, D13)
Tests first:
- [x] A finished match disappears from the summary
- [x] Unknown or already finished match → `MatchNotFoundException`
- [x] Updating a finished match fails
- [x] The same pairing can be started again after it finishes
- [x] Both teams are free after finishing: Poland–Spain finished, then Poland–Mexico and Spain–Germany can start
- [x] `updateScore` and `finishMatch` match team names ignoring case and surrounding whitespace (`" poland"`, `"SPAIN "`)
- [x] Home and away are kept apart: if Poland–Spain is live, `finishMatch("Spain", "Poland")` → `MatchNotFoundException`, and the live match stays on the board

Implementation:
- [x] `finishMatch(home, away)`
- [x] Commit: *Add finishing a match*

## Step 5: Summary ordering (D15, D16)
Tests first:
- [x] Ordered by total score, highest first
- [x] A tie goes to the match started most recently
- [x] A score update doesn't change the tie-break
- [x] Totals beyond `int` range are ordered correctly (total computed as `long`, no overflow)
- [x] A rematch counts as most recently started: Mexico–Canada finished and restarted after Spain–Brazil comes first
- [x] The returned list can't be modified
- [x] Snapshots don't change: a summary taken before an update still shows the old score
- [x] Acceptance test with the example from the task description

Implementation:
- [x] Start sequence counter and comparator
- [x] Commit: *Order summary by total score and start order*

## Step 6: Thread safety (D17)
- [x] Concurrency smoke test: many threads start, update and finish matches; afterwards no team is in two matches and the state is consistent
- [x] `synchronized` public methods; thread safety documented in class Javadoc
- [x] Commit: *Make Scoreboard thread-safe*

## Step 7: README.md
- [x] Usage example
- [x] Assumptions, reasoning and trade-offs (summarised from DECISIONS.md, with a link)
- [x] Thread-safety upgrade path
- [x] How to build and test
- [x] Commit: *Add README*

## Step 8: Extra feature, `findMatchByTeam` (D22), in its own commit
Tests first:
- [x] Finds a match whether the team is home or away
- [x] Ignores case and surrounding spaces
- [x] Returns empty for an unknown team or a finished match
- [x] Blank input → `IllegalArgumentException`

Implementation:
- [x] `Optional<Match> findMatchByTeam(String team)`
- [x] README section: what the feature does and why it was chosen
- [x] Commit: *Add finding a match by team*

## Step 9: AI.md (D23)
- [ ] Summary of how AI was used
- [ ] Full prompt history
- [ ] Artifacts that guided the work (DECISIONS.md, PLAN.md)
- [ ] Commit: *Add AI.md*
