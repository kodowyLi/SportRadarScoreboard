package com.sportradar.scoreboard.exception;

import java.io.Serial;

/**
 * Thrown when a home/away pairing does not identify a match in progress.
 */
public final class MatchNotFoundException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Home team of the requested match, normalised. */
    private final String homeTeam;
    /** Away team of the requested match, normalised. */
    private final String awayTeam;

    /**
     * Creates the exception for a pairing that is not in progress.
     *
     * @param homeTeam home team of the requested match, normalised
     * @param awayTeam away team of the requested match, normalised
     */
    public MatchNotFoundException(String homeTeam, String awayTeam) {
        super("No match in progress: " + homeTeam + " - " + awayTeam);
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
