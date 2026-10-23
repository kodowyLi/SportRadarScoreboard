package com.sportradar.scoreboard;

import com.sportradar.scoreboard.exception.MatchAlreadyStartedException;
import com.sportradar.scoreboard.exception.TeamAlreadyPlayingException;

import java.util.List;

/**
 * Live football scoreboard that keeps track of matches in progress.
 * <p>
 * Team names are stripped of surrounding whitespace and compared ignoring case, so {@code "Spain"} and {@code " spain "}
 * refer to the same team. A team can take part in at most one match in progress at a time.
 */
public interface Scoreboard {

    /**
     * Starts a new match with an initial score of 0–0.
     *
     * @param homeTeam home team name
     * @param awayTeam away team name
     * @return snapshot of the started match
     * @throws IllegalArgumentException     if a team name is null or blank, or both names refer to the same team
     * @throws MatchAlreadyStartedException if the same home/away pairing is already in progress
     * @throws TeamAlreadyPlayingException  if either team is already playing in another match
     */
    Match startMatch(String homeTeam, String awayTeam);

    /**
     * Returns the matches currently in progress.
     *
     * @return unmodifiable list of match snapshots
     */
    List<Match> getSummary();
}
