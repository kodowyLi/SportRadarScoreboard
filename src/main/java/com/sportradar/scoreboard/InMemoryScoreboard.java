package com.sportradar.scoreboard;

import com.sportradar.scoreboard.exception.MatchAlreadyStartedException;
import com.sportradar.scoreboard.exception.MatchNotFoundException;
import com.sportradar.scoreboard.exception.TeamAlreadyPlayingException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Scoreboard} that keeps all matches in memory.
 */
public final class InMemoryScoreboard implements Scoreboard {

    private final Map<MatchKey, Match> matches = new LinkedHashMap<>();

    @Override
    public Match startMatch(String homeTeam, String awayTeam) {
        TeamName home = TeamName.of(homeTeam);
        TeamName away = TeamName.of(awayTeam);
        if (home.sameTeamAs(away)) {
            throw new IllegalArgumentException("A team cannot play against itself: " + home.display());
        }
        MatchKey key = new MatchKey(home.key(), away.key());
        if (matches.containsKey(key)) {
            throw new MatchAlreadyStartedException(home.display(), away.display());
        }
        requireNotPlaying(home);
        requireNotPlaying(away);

        Match match = new Match(home.display(), away.display(), 0, 0);
        matches.put(key, match);
        return match;
    }

    @Override
    public void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        TeamName home = TeamName.of(homeTeam);
        TeamName away = TeamName.of(awayTeam);
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException(
                    "Scores must not be negative: " + homeScore + " - " + awayScore);
        }
        MatchKey key = new MatchKey(home.key(), away.key());
        Match current = matches.get(key);
        if (current == null) {
            throw new MatchNotFoundException(home.display(), away.display());
        }
        matches.put(key, new Match(current.homeTeam(), current.awayTeam(), homeScore, awayScore));
    }

    @Override
    public List<Match> getSummary() {
        return List.copyOf(matches.values());
    }

    private void requireNotPlaying(TeamName team) {
        boolean playing = matches.keySet().stream().anyMatch(key -> key.involves(team.key()));
        if (playing) {
            throw new TeamAlreadyPlayingException(team.display());
        }
    }

    // Separate fields rather than a joined string, so "AB"/"CDE" and "ABC"/"DE" never collide.
    private record MatchKey(String homeTeam, String awayTeam) {

        boolean involves(String teamKey) {
            return homeTeam.equals(teamKey) || awayTeam.equals(teamKey);
        }
    }
}
