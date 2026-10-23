package com.sportradar.scoreboard;

/**
 * Immutable snapshot of a match on the scoreboard.
 *
 * @param homeTeam  home team name, as given when the match was started, with whitespace normalised
 * @param awayTeam  away team name, as given when the match was started, with whitespace normalised
 * @param homeScore goals scored by the home team
 * @param awayScore goals scored by the away team
 */
public record Match(String homeTeam, String awayTeam, int homeScore, int awayScore) {

    /**
     * Creates a match snapshot.
     *
     * @throws IllegalArgumentException if a team name is null or blank, or a score is negative
     */
    public Match {
        if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
            throw new IllegalArgumentException("Team names must not be null or blank");
        }
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException(
                    "Scores must not be negative: " + homeScore + " - " + awayScore);
        }
    }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayTeam + " " + awayScore;
    }
}
