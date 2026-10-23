package com.sportradar.scoreboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchTest {

    @Test
    void formatsAsHomeTeamScoreDashAwayTeamScore() {
        Match match = new Match("Uruguay", "Italy", 6, 6);

        assertThat(match).hasToString("Uruguay 6 - Italy 6");
    }

    @Test
    void acceptsZeroScores() {
        assertThat(new Match("Mexico", "Canada", 0, 0).homeScore()).isZero();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsMissingHomeTeam(String homeTeam) {
        assertThatThrownBy(() -> new Match(homeTeam, "Canada", 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsMissingAwayTeam(String awayTeam) {
        assertThatThrownBy(() -> new Match("Mexico", awayTeam, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1"})
    void rejectsNegativeScore(int homeScore, int awayScore) {
        assertThatThrownBy(() -> new Match("Mexico", "Canada", homeScore, awayScore))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
