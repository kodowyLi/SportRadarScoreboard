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

import java.util.List;

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
        void matchAlreadyStartedExceptionNamesTheTeams() {
            scoreboard.startMatch("Spain", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch(" Spain", "Brazil "))
                    .hasMessage("Match already in progress: Spain - Brazil")
                    .isInstanceOfSatisfying(MatchAlreadyStartedException.class, e -> {
                        assertThat(e.getHomeTeam()).isEqualTo("Spain");
                        assertThat(e.getAwayTeam()).isEqualTo("Brazil");
                    });
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
        void teamAlreadyPlayingExceptionNamesTheTeam() {
            scoreboard.startMatch("Poland", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Spain", "Poland"))
                    .hasMessage("Team is already playing in another match: Poland")
                    .isInstanceOfSatisfying(TeamAlreadyPlayingException.class,
                            e -> assertThat(e.getTeam()).isEqualTo("Poland"));
        }

        @Test
        void rejectsAwayTeamAlreadyPlayingInAnotherMatch() {
            scoreboard.startMatch("Poland", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Spain", "poland"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
        }

        @Test
        void treatsWhitespaceVariantsInsideNameAsSameTeam() {
            scoreboard.startMatch("Costa\u00A0Rica", "Mexico");

            assertThatThrownBy(() -> scoreboard.startMatch("Costa Rica", "Canada"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
        }

        @Test
        void treatsComposedAndDecomposedAccentsAsSameTeam() {
            scoreboard.startMatch("Cura\u00E7ao", "Mexico");

            assertThatThrownBy(() -> scoreboard.startMatch("Curac\u0327ao", "Canada"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
        }

        @Test
        void treatsNameWithZeroWidthSpaceAsSameTeam() {
            scoreboard.startMatch("Spain", "Brazil");

            assertThatThrownBy(() -> scoreboard.startMatch("Spain\u200B", "Germany"))
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
        void matchNotFoundExceptionNamesTheTeams() {
            assertThatThrownBy(() -> scoreboard.updateScore("Spain", "Brazil", 1, 0))
                    .hasMessage("No match in progress: Spain - Brazil")
                    .isInstanceOfSatisfying(MatchNotFoundException.class, e -> {
                        assertThat(e.getHomeTeam()).isEqualTo("Spain");
                        assertThat(e.getAwayTeam()).isEqualTo("Brazil");
                    });
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void rejectsMissingTeamName(String team) {
            scoreboard.startMatch("Mexico", "Canada");

            assertThatThrownBy(() -> scoreboard.updateScore(team, "Canada", 1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> scoreboard.updateScore("Mexico", team, 1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void validatesArgumentsBeforeLookingUpMatch() {
            assertThatThrownBy(() -> scoreboard.updateScore("Spain", "Brazil", -1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
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

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void rejectsMissingTeamName(String team) {
            scoreboard.startMatch("Mexico", "Canada");

            assertThatThrownBy(() -> scoreboard.finishMatch(team, "Canada"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> scoreboard.finishMatch("Mexico", team))
                    .isInstanceOf(IllegalArgumentException.class);
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

    @Nested
    class Summary {

        @Test
        void ordersByTotalScoreDescending() {
            startWithScore("Mexico", "Canada", 0, 1);
            startWithScore("Spain", "Brazil", 3, 2);
            startWithScore("Germany", "France", 1, 1);

            assertThat(scoreboard.getSummary()).containsExactly(
                    new Match("Spain", "Brazil", 3, 2),
                    new Match("Germany", "France", 1, 1),
                    new Match("Mexico", "Canada", 0, 1));
        }

        @Test
        void ordersTiesByMostRecentlyStartedFirst() {
            startWithScore("Mexico", "Canada", 1, 1);
            startWithScore("Spain", "Brazil", 2, 0);
            startWithScore("Germany", "France", 0, 2);

            assertThat(scoreboard.getSummary()).containsExactly(
                    new Match("Germany", "France", 0, 2),
                    new Match("Spain", "Brazil", 2, 0),
                    new Match("Mexico", "Canada", 1, 1));
        }

        @Test
        void scoreUpdateDoesNotChangeTieBreak() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");
            scoreboard.updateScore("Spain", "Brazil", 1, 1);

            scoreboard.updateScore("Mexico", "Canada", 1, 1);

            assertThat(scoreboard.getSummary()).containsExactly(
                    new Match("Spain", "Brazil", 1, 1),
                    new Match("Mexico", "Canada", 1, 1));
        }

        @Test
        void ordersTotalsBeyondIntRangeCorrectly() {
            startWithScore("Mexico", "Canada", 1, 0);
            startWithScore("Spain", "Brazil", Integer.MAX_VALUE, 1);
            startWithScore("Germany", "France", Integer.MAX_VALUE, Integer.MAX_VALUE);

            assertThat(scoreboard.getSummary()).containsExactly(
                    new Match("Germany", "France", Integer.MAX_VALUE, Integer.MAX_VALUE),
                    new Match("Spain", "Brazil", Integer.MAX_VALUE, 1),
                    new Match("Mexico", "Canada", 1, 0));
        }

        @Test
        void rematchCountsAsMostRecentlyStarted() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");
            scoreboard.finishMatch("Mexico", "Canada");

            scoreboard.startMatch("Mexico", "Canada");

            assertThat(scoreboard.getSummary()).containsExactly(
                    new Match("Mexico", "Canada", 0, 0),
                    new Match("Spain", "Brazil", 0, 0));
        }

        @Test
        void isUnmodifiable() {
            scoreboard.startMatch("Mexico", "Canada");
            List<Match> summary = scoreboard.getSummary();

            assertThatThrownBy(() -> summary.add(new Match("Spain", "Brazil", 0, 0)))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(summary::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThat(scoreboard.getSummary()).hasSize(1);
        }

        @Test
        void isSnapshotNotAffectedByLaterChanges() {
            scoreboard.startMatch("Mexico", "Canada");
            List<Match> before = scoreboard.getSummary();

            scoreboard.updateScore("Mexico", "Canada", 0, 5);
            scoreboard.startMatch("Spain", "Brazil");

            assertThat(before).containsExactly(new Match("Mexico", "Canada", 0, 0));
        }

        @Test
        void matchesExampleFromTaskDescription() {
            startWithScore("Mexico", "Canada", 0, 5);
            startWithScore("Spain", "Brazil", 10, 2);
            startWithScore("Germany", "France", 2, 2);
            startWithScore("Uruguay", "Italy", 6, 6);
            startWithScore("Argentina", "Australia", 3, 1);

            assertThat(scoreboard.getSummary()).containsExactly(
                    new Match("Uruguay", "Italy", 6, 6),
                    new Match("Spain", "Brazil", 10, 2),
                    new Match("Mexico", "Canada", 0, 5),
                    new Match("Argentina", "Australia", 3, 1),
                    new Match("Germany", "France", 2, 2));
        }

        private void startWithScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
            scoreboard.startMatch(homeTeam, awayTeam);
            scoreboard.updateScore(homeTeam, awayTeam, homeScore, awayScore);
        }
    }

    @Nested
    class FindMatchByTeam {

        @Test
        void findsMatchByHomeTeam() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");
            scoreboard.updateScore("Spain", "Brazil", 2, 1);

            assertThat(scoreboard.findMatchByTeam("Spain"))
                    .contains(new Match("Spain", "Brazil", 2, 1));
        }

        @Test
        void findsMatchByAwayTeam() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");
            scoreboard.updateScore("Spain", "Brazil", 2, 1);

            assertThat(scoreboard.findMatchByTeam("Brazil"))
                    .contains(new Match("Spain", "Brazil", 2, 1));
        }

        @Test
        void matchesTeamNameIgnoringCaseAndSurroundingWhitespace() {
            scoreboard.startMatch("Poland", "Spain");

            assertThat(scoreboard.findMatchByTeam(" sPAIN\u00A0"))
                    .contains(new Match("Poland", "Spain", 0, 0));
        }

        @Test
        void returnsEmptyForUnknownTeam() {
            scoreboard.startMatch("Mexico", "Canada");

            assertThat(scoreboard.findMatchByTeam("Spain")).isEmpty();
        }

        @Test
        void returnsEmptyForTeamWhoseMatchIsFinished() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.finishMatch("Mexico", "Canada");

            assertThat(scoreboard.findMatchByTeam("Mexico")).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\u00A0"})
        void rejectsMissingTeamName(String team) {
            assertThatThrownBy(() -> scoreboard.findMatchByTeam(team))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
