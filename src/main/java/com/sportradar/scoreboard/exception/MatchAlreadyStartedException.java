package com.sportradar.scoreboard.exception;

/**
 * Thrown when starting a match whose home/away pairing is already in progress.
 */
public class MatchAlreadyStartedException extends IllegalStateException {

    public MatchAlreadyStartedException(String homeTeam, String awayTeam) {
        super("Match already in progress: " + homeTeam + " - " + awayTeam);
    }
}
