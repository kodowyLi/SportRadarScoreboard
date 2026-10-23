package com.sportradar.scoreboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamNameTest {

    @Test
    void stripsSurroundingWhitespaceForDisplay() {
        TeamName team = TeamName.of("  Spain\t");

        assertThat(team.display()).isEqualTo("Spain");
    }

    @Test
    void stripsUnicodeWhitespace() {
        TeamName team = TeamName.of("\u2003Spain\u3000");

        assertThat(team.display()).isEqualTo("Spain");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Spain\u00A0", "\u00A0Spain", "\u2007Spain\u202F", "\u00A0 Spain \u00A0"})
    void stripsNonBreakingSpaces(String rawName) {
        TeamName team = TeamName.of(rawName);

        assertThat(team.display()).isEqualTo("Spain");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Costa\u00A0Rica", "Costa  Rica", "Costa\tRica", " Costa\u2003 \u202FRica "})
    void collapsesWhitespaceInsideNameToSingleSpace(String rawName) {
        TeamName team = TeamName.of(rawName);

        assertThat(team.display()).isEqualTo("Costa Rica");
    }

    @Test
    void sameTeamRegardlessOfWhitespaceInsideName() {
        assertThat(TeamName.of("Costa\u00A0Rica").sameTeamAs(TeamName.of("costa  RICA"))).isTrue();
    }

    @Test
    void keepsOriginalCaseForDisplay() {
        TeamName team = TeamName.of("SPAIN");

        assertThat(team.display()).isEqualTo("SPAIN");
    }

    @Test
    void keyIgnoresCase() {
        assertThat(TeamName.of("SPAIN").key()).isEqualTo(TeamName.of("spain").key());
    }

    @Test
    void sameTeamIgnoringCaseAndWhitespace() {
        assertThat(TeamName.of("Spain").sameTeamAs(TeamName.of("\u2003sPaIn "))).isTrue();
    }

    @Test
    void differentTeams() {
        assertThat(TeamName.of("Spain").sameTeamAs(TeamName.of("Brazil"))).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\u2003", "\u3000 \n", "\u00A0", "\u2007\u202F"})
    void rejectsNullAndBlankNames(String rawName) {
        assertThatThrownBy(() -> TeamName.of(rawName))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
