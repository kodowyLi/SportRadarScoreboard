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

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayTeam + " " + awayScore;
    }
}
