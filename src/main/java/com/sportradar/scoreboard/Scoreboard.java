package com.sportradar.scoreboard;

import com.sportradar.scoreboard.exception.MatchAlreadyStartedException;
import com.sportradar.scoreboard.exception.MatchNotFoundException;
import com.sportradar.scoreboard.exception.TeamAlreadyPlayingException;

import java.util.List;
import java.util.Optional;

/**
 * Live football scoreboard that keeps track of matches in progress.
 * <p>
 * Team names are normalised: surrounding whitespace is removed, any whitespace inside a name
 * becomes a single space, and names are compared ignoring case. So {@code "Costa Rica"},
 * {@code " costa  RICA "} and a name using a no-break space all refer to the same team.
 * A team can take part in at most one match in progress at a time.
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
     * Replaces the score of a match in progress with the given absolute score. A lower score than
     * the current one is accepted, e.g. to apply a correction.
     *
     * @param homeTeam  home team of the match
     * @param awayTeam  away team of the match
     * @param homeScore new home team score
     * @param awayScore new away team score
     * @throws IllegalArgumentException if a team name is null or blank, or a score is negative
     * @throws MatchNotFoundException   if no match with this home/away pairing is in progress
     */
    void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore);

    /**
     * Finishes a match in progress and removes it from the scoreboard. The same pairing can be
     * started again afterwards.
     *
     * @param homeTeam home team of the match
     * @param awayTeam away team of the match
     * @throws IllegalArgumentException if a team name is null or blank
     * @throws MatchNotFoundException   if no match with this home/away pairing is in progress
     */
    void finishMatch(String homeTeam, String awayTeam);

    /**
     * Returns the matches currently in progress, ordered by total score (highest first). Matches
     * with the same total score are ordered by start order, most recently started first; score
     * updates do not affect this order.
     *
     * @return unmodifiable list of match snapshots
     */
    List<Match> getSummary();

    /**
     * Finds the match in progress that the given team is playing in, either at home or away.
     * Since a team plays at most one match at a time, there is at most one result.
     *
     * @param team team name
     * @return snapshot of the team's match in progress, or empty if the team is not playing
     * @throws IllegalArgumentException if the team name is null or blank
     */
    Optional<Match> findMatchByTeam(String team);
}
