package com.sportradar.scoreboard.exception;

/**
 * Thrown when a home/away pairing does not identify a match in progress.
 */
public class MatchNotFoundException extends IllegalStateException {

    /** Home team of the requested match, normalised. */
    private final String homeTeam;
    /** Away team of the requested match, normalised. */
    private final String awayTeam;

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
