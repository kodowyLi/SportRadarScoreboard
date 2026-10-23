package com.sportradar.scoreboard.exception;

/**
 * Thrown when a home/away pairing does not identify a match in progress.
 */
public class MatchNotFoundException extends IllegalStateException {

    public MatchNotFoundException(String homeTeam, String awayTeam) {
        super("No match in progress: " + homeTeam + " - " + awayTeam);
    }
}
