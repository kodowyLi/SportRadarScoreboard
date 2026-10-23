package com.sportradar.scoreboard.exception;

/**
 * Thrown when starting a match whose home/away pairing is already in progress.
 */
public class MatchAlreadyStartedException extends IllegalStateException {

    /** Home team of the requested match, normalised. */
    private final String homeTeam;
    /** Away team of the requested match, normalised. */
    private final String awayTeam;

    public MatchAlreadyStartedException(String homeTeam, String awayTeam) {
        super("Match already in progress: " + homeTeam + " - " + awayTeam);
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    /**
     * Returns the home team of the requested match.
     *
     * @return home team name, normalised
     */
    public String getHomeTeam() {
        return homeTeam;
    }

    /**
     * Returns the away team of the requested match.
     *
     * @return away team name, normalised
     */
    public String getAwayTeam() {
        return awayTeam;
    }
}
