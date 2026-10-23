package com.sportradar.scoreboard;

import com.sportradar.scoreboard.exception.MatchAlreadyStartedException;
import com.sportradar.scoreboard.exception.MatchNotFoundException;
import com.sportradar.scoreboard.exception.TeamAlreadyPlayingException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryScoreboardTest {

    private final Scoreboard scoreboard = new InMemoryScoreboard();

    @Nested
    class StartMatch {

        @Test
        void returnsNewMatchWithZeroZeroScore() {
            Match match = scoreboard.startMatch("Mexico", "Canada");

            assertThat(match).isEqualTo(new Match("Mexico", "Canada", 0, 0));
        }

        @Test
        void addsMatchToSummary() {
            scoreboard.startMatch("Mexico", "Canada");

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Mexico", "Canada", 0, 0));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void rejectsMissingHomeTeam(String homeTeam) {
            assertThatThrownBy(() -> scoreboard.startMatch(homeTeam, "Canada"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void rejectsMissingAwayTeam(String awayTeam) {
            assertThatThrownBy(() -> scoreboard.startMatch("Mexico", awayTeam))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsTeamPlayingItself() {
            assertThatThrownBy(() -> scoreboard.startMatch("Spain", " spain "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void trimsTeamNames() {
            Match match = scoreboard.startMatch("  Spain ", " Brazil  ");

            assertThat(match).isEqualTo(new Match("Spain", "Brazil", 0, 0));
        }

        @Test
        void rejectsSamePairingWhileInProgress() {
            scoreboard.startMatch("Spain", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Spain", "Brazil"))
                    .isInstanceOf(MatchAlreadyStartedException.class);
        }

        @Test
        void comparesTeamNamesIgnoringCaseAndSurroundingWhitespace() {
            scoreboard.startMatch("Spain", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch(" SPAIN", "brazil "))
                    .isInstanceOf(MatchAlreadyStartedException.class);
        }

        @Test
        void rejectsHomeTeamAlreadyPlayingInAnotherMatch() {
            scoreboard.startMatch("Poland", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Poland", "Spain"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
        }

        @Test
        void rejectsAwayTeamAlreadyPlayingInAnotherMatch() {
            scoreboard.startMatch("Poland", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Spain", "poland"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
        }

        @Test
        void rejectsReversedPairingWhileInProgress() {
            scoreboard.startMatch("Spain", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Brazil", "Spain"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
        }

        @Test
        void rejectedStartLeavesScoreboardUnchanged() {
            scoreboard.startMatch("Poland", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Spain", "Poland"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Poland", "Brazil", 0, 0));
        }

        @Test
        void distinguishesMatchesWhoseConcatenatedTeamNamesAreEqual() {
            scoreboard.startMatch("AB", "CDE");
            scoreboard.startMatch("ABC", "DE");

            assertThat(scoreboard.getSummary()).containsExactlyInAnyOrder(
                    new Match("AB", "CDE", 0, 0),
                    new Match("ABC", "DE", 0, 0));
        }
    }

    @Nested
    class UpdateScore {

        @Test
        void setsAbsoluteScore() {
            scoreboard.startMatch("Mexico", "Canada");

            scoreboard.updateScore("Mexico", "Canada", 0, 5);

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Mexico", "Canada", 0, 5));
        }

        @Test
        void allowsLoweringScore() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.updateScore("Mexico", "Canada", 2, 5);

            scoreboard.updateScore("Mexico", "Canada", 1, 4);

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Mexico", "Canada", 1, 4));
        }

        @ParameterizedTest
        @CsvSource({"-1, 0", "0, -1", "-1, -1"})
        void rejectsNegativeScore(int homeScore, int awayScore) {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.updateScore("Mexico", "Canada", 1, 1);

            assertThatThrownBy(() -> scoreboard.updateScore("Mexico", "Canada", homeScore, awayScore))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Mexico", "Canada", 1, 1));
        }

        @Test
        void rejectsUnknownMatch() {
            scoreboard.startMatch("Mexico", "Canada");

            assertThatThrownBy(() -> scoreboard.updateScore("Spain", "Brazil", 1, 0))
                    .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        void matchesTeamNamesIgnoringCaseAndSurroundingWhitespace() {
            scoreboard.startMatch("Poland", "Spain");

            scoreboard.updateScore(" poland", "SPAIN ", 2, 1);

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Poland", "Spain", 2, 1));
        }

        @Test
        void rejectsSwappedTeams() {
            scoreboard.startMatch("Poland", "Spain");
            scoreboard.updateScore("Poland", "Spain", 2, 1);

            assertThatThrownBy(() -> scoreboard.updateScore("Spain", "Poland", 1, 2))
                    .isInstanceOf(MatchNotFoundException.class);

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Poland", "Spain", 2, 1));
        }
    }

    @Nested
    class FinishMatch {

        @Test
        void removesMatchFromSummary() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");

            scoreboard.finishMatch("Mexico", "Canada");

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Spain", "Brazil", 0, 0));
        }

        @Test
        void rejectsUnknownMatch() {
            scoreboard.startMatch("Mexico", "Canada");

            assertThatThrownBy(() -> scoreboard.finishMatch("Spain", "Brazil"))
                    .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        void rejectsAlreadyFinishedMatch() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.finishMatch("Mexico", "Canada");

            assertThatThrownBy(() -> scoreboard.finishMatch("Mexico", "Canada"))
                    .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        void finishedMatchCannotBeUpdated() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.finishMatch("Mexico", "Canada");

            assertThatThrownBy(() -> scoreboard.updateScore("Mexico", "Canada", 1, 0))
                    .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        void samePairingCanBeStartedAgainAfterFinishing() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.updateScore("Mexico", "Canada", 2, 1);
            scoreboard.finishMatch("Mexico", "Canada");

            Match rematch = scoreboard.startMatch("Mexico", "Canada");

            assertThat(rematch).isEqualTo(new Match("Mexico", "Canada", 0, 0));
            assertThat(scoreboard.getSummary()).containsExactly(rematch);
        }

        @Test
        void teamsCanPlayOtherMatchesAfterFinishing() {
            scoreboard.startMatch("Poland", "Spain");
            scoreboard.finishMatch("Poland", "Spain");

            scoreboard.startMatch("Poland", "Mexico");
            scoreboard.startMatch("Spain", "Germany");

            assertThat(scoreboard.getSummary()).containsExactlyInAnyOrder(
                    new Match("Poland", "Mexico", 0, 0),
                    new Match("Spain", "Germany", 0, 0));
        }

        @Test
        void matchesTeamNamesIgnoringCaseAndSurroundingWhitespace() {
            scoreboard.startMatch("Poland", "Spain");
            scoreboard.startMatch("Mexico", "Canada");

            scoreboard.finishMatch(" poland", "SPAIN ");

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Mexico", "Canada", 0, 0));
        }

        @Test
        void rejectsSwappedTeams() {
            scoreboard.startMatch("Poland", "Spain");

            assertThatThrownBy(() -> scoreboard.finishMatch("Spain", "Poland"))
                    .isInstanceOf(MatchNotFoundException.class);

            assertThat(scoreboard.getSummary())
                    .containsExactly(new Match("Poland", "Spain", 0, 0));
        }
    }
}
