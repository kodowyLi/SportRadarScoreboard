package com.sportradar.scoreboard;

import com.sportradar.scoreboard.exception.MatchAlreadyStartedException;
import com.sportradar.scoreboard.exception.MatchNotFoundException;
import com.sportradar.scoreboard.exception.TeamAlreadyPlayingException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link Scoreboard} that keeps all matches in memory.
 * <p>
 * This class is thread-safe: every public method is {@code synchronized} on the instance, so
 * each operation is atomic, including the checks performed before a match is started, and
 * {@link #getSummary()} always reflects a single consistent state.
 */
public final class InMemoryScoreboard implements Scoreboard {

    private static final Comparator<LiveMatch> SUMMARY_ORDER = Comparator
            .comparingLong(LiveMatch::totalScore).reversed()
            .thenComparing(Comparator.comparingLong(LiveMatch::startSequence).reversed());

    private final Map<MatchKey, LiveMatch> matches = new HashMap<>();
    private long nextStartSequence;

    @Override
    public synchronized Match startMatch(String homeTeam, String awayTeam) {
        TeamName home = new TeamName(homeTeam);
        TeamName away = new TeamName(awayTeam);
        if (home.equals(away)) {
            throw new IllegalArgumentException("A team cannot play against itself: " + home.display());
        }
        MatchKey key = new MatchKey(home.key(), away.key());
        if (matches.containsKey(key)) {
            throw new MatchAlreadyStartedException(home.display(), away.display());
        }
        requireNotPlaying(home);
        requireNotPlaying(away);

        Match match = new Match(home.display(), away.display(), 0, 0);
        matches.put(key, new LiveMatch(match, nextStartSequence++));
        return match;
    }

    @Override
    public synchronized void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        TeamName home = new TeamName(homeTeam);
        TeamName away = new TeamName(awayTeam);
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException(
                    "Scores must not be negative: " + homeScore + " - " + awayScore);
        }
        MatchKey key = new MatchKey(home.key(), away.key());
        LiveMatch current = matches.get(key);
        if (current == null) {
            throw new MatchNotFoundException(home.display(), away.display());
        }
        matches.put(key, current.withScore(homeScore, awayScore));
    }

    @Override
    public synchronized void finishMatch(String homeTeam, String awayTeam) {
        TeamName home = new TeamName(homeTeam);
        TeamName away = new TeamName(awayTeam);
        if (matches.remove(new MatchKey(home.key(), away.key())) == null) {
            throw new MatchNotFoundException(home.display(), away.display());
        }
    }

    @Override
    public synchronized List<Match> getSummary() {
        return matches.values().stream()
                .sorted(SUMMARY_ORDER)
                .map(LiveMatch::match)
                .toList();
    }

    @Override
    public synchronized Optional<Match> findMatchByTeam(String team) {
        String teamKey = new TeamName(team).key();
        return matches.entrySet().stream()
                .filter(entry -> entry.getKey().involves(teamKey))
                .map(entry -> entry.getValue().match())
                .findFirst();
    }

    private void requireNotPlaying(TeamName team) {
        String teamKey = team.key();
        boolean playing = matches.keySet().stream().anyMatch(key -> key.involves(teamKey));
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

    // A monotonic sequence rather than a timestamp, so tie-breaks are deterministic.
    private record LiveMatch(Match match, long startSequence) {

        LiveMatch withScore(int homeScore, int awayScore) {
            return new LiveMatch(
                    new Match(match.homeTeam(), match.awayTeam(), homeScore, awayScore), startSequence);
        }

        // long, so that two large int scores cannot overflow into a negative total
        long totalScore() {
            return (long) match.homeScore() + match.awayScore();
        }
    }
}
