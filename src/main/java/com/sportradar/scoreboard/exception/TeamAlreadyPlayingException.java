package com.sportradar.scoreboard.exception;

/**
 * Thrown when starting a match for a team that is already playing in another match in progress.
 */
public class TeamAlreadyPlayingException extends IllegalStateException {

    public TeamAlreadyPlayingException(String team) {
        super("Team is already playing in another match: " + team);
    }
}
