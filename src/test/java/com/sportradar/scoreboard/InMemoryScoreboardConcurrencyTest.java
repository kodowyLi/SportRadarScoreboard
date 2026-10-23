package com.sportradar.scoreboard;

import com.sportradar.scoreboard.exception.TeamAlreadyPlayingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

// Smoke tests: they can reveal races but cannot prove their absence.
@Timeout(30)
class InMemoryScoreboardConcurrencyTest {

    private static final int THREADS = 8;

    @Test
    void onlyOneConcurrentStartForTheSameTeamSucceeds() {
        for (int round = 0; round < 200; round++) {
            Scoreboard scoreboard = new InMemoryScoreboard();
            AtomicInteger started = new AtomicInteger();
            Queue<Throwable> unexpected = new ConcurrentLinkedQueue<>();

            runConcurrently(THREADS, thread -> {
                try {
                    scoreboard.startMatch("Spain", "Opponent " + thread);
                    started.incrementAndGet();
                } catch (TeamAlreadyPlayingException expected) {
                    // another thread won the race
                } catch (Throwable e) {
                    unexpected.add(e);
                }
            });

            assertThat(unexpected).isEmpty();
            assertThat(started).hasValue(1);
            assertThat(scoreboard.getSummary()).hasSize(1);
        }
    }

    @Test
    void concurrentStartsOfDifferentMatchesAreAllRecorded() {
        Scoreboard scoreboard = new InMemoryScoreboard();
        int matchesPerThread = 500;

        runConcurrently(THREADS, thread -> {
            for (int i = 0; i < matchesPerThread; i++) {
                scoreboard.startMatch("Home " + thread + "/" + i, "Away " + thread + "/" + i);
            }
        });

        assertThat(scoreboard.getSummary()).hasSize(THREADS * matchesPerThread);
    }

    @Test
    void concurrentMatchLifecyclesKeepScoreboardConsistent() {
        Scoreboard scoreboard = new InMemoryScoreboard();
        Queue<Throwable> errors = new ConcurrentLinkedQueue<>();
        int readers = 2;
        CountDownLatch writersRunning = new CountDownLatch(THREADS);

        runConcurrently(THREADS + readers, thread -> {
            try {
                if (thread < readers) {
                    while (writersRunning.getCount() > 0) {
                        assertNoTeamPlaysTwice(scoreboard.getSummary());
                    }
                } else {
                    try {
                        playMatches(scoreboard, thread);
                    } finally {
                        writersRunning.countDown();
                    }
                }
            } catch (Throwable e) {
                errors.add(e);
            }
        });

        assertThat(errors).isEmpty();
        assertThat(scoreboard.getSummary()).isEmpty();
    }

    private static void playMatches(Scoreboard scoreboard, int thread) {
        String home = "Home " + thread;
        String away = "Away " + thread;
        for (int i = 0; i < 2_000; i++) {
            scoreboard.startMatch(home, away);
            scoreboard.updateScore(home, away, i % 10, i % 7);
            scoreboard.finishMatch(home, away);
        }
    }

    private static void assertNoTeamPlaysTwice(List<Match> summary) {
        Set<String> teams = new HashSet<>();
        for (Match match : summary) {
            assertThat(teams.add(match.homeTeam())).as("%s plays twice", match.homeTeam()).isTrue();
            assertThat(teams.add(match.awayTeam())).as("%s plays twice", match.awayTeam()).isTrue();
        }
    }

    // Releases all tasks at the same moment to maximise contention, waits for them to finish and
    // rethrows the first failure.
    private static void runConcurrently(int threads, IntConsumer task) {
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int thread = 0; thread < threads; thread++) {
                int id = thread;
                futures.add(executor.submit(() -> {
                    awaitUninterruptibly(startGate);
                    task.accept(id);
                }));
            }
            startGate.countDown();
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new AssertionError("Concurrent task failed", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }

    private static void awaitUninterruptibly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
