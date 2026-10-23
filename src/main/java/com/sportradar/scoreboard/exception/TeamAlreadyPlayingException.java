package com.sportradar.scoreboard.exception;

/**
 * Thrown when starting a match for a team that is already playing in another match in progress.
 */
public class TeamAlreadyPlayingException extends IllegalStateException {

    /** The team that is already playing, normalised. */
    private final String team;

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
