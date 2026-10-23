package com.sportradar.scoreboard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchTest {

    @Test
    void formatsAsHomeTeamScoreDashAwayTeamScore() {
        Match match = new Match("Uruguay", "Italy", 6, 6);

        assertThat(match).hasToString("Uruguay 6 - Italy 6");
    }
}
