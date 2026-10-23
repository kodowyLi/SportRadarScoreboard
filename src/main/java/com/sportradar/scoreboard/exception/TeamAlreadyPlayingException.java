package com.sportradar.scoreboard.exception;

import java.io.Serial;

/**
 * Thrown when starting a match for a team that is already playing in another match in progress.
 */
public final class TeamAlreadyPlayingException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The team that is already playing, normalised. */
    private final String team;

    /**
     * Creates the exception for a team that is already playing.
     *
     * @param team the team that is already playing, normalised
     */
    public TeamAlreadyPlayingException(String team) {
        super("Team is already playing in another match: " + team);
        this.team = team;
    }

    /**
     * Returns the team that is already playing in another match.
     *
     * @return team name, normalised
     */
    public String getTeam() {
        return team;
    }
}
