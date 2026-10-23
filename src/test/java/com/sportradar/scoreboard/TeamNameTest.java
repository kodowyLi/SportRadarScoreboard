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
        TeamName team = new TeamName("  Spain\t");

        assertThat(team.display()).isEqualTo("Spain");
    }

    @Test
    void stripsUnicodeWhitespace() {
        TeamName team = new TeamName("\u2003Spain\u3000");

        assertThat(team.display()).isEqualTo("Spain");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Spain\u00A0", "\u00A0Spain", "\u2007Spain\u202F", "\u00A0 Spain \u00A0"})
    void stripsNonBreakingSpaces(String rawName) {
        TeamName team = new TeamName(rawName);

        assertThat(team.display()).isEqualTo("Spain");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Costa\u00A0Rica", "Costa  Rica", "Costa\tRica", " Costa\u2003 \u202FRica "})
    void collapsesWhitespaceInsideNameToSingleSpace(String rawName) {
        TeamName team = new TeamName(rawName);

        assertThat(team.display()).isEqualTo("Costa Rica");
    }

    @Test
    void equalRegardlessOfWhitespaceInsideName() {
        assertThat(new TeamName("Costa\u00A0Rica")).isEqualTo(new TeamName("costa  RICA"));
    }

    @Test
    void composedAndDecomposedAccentsAreSameTeam() {
        TeamName composed = new TeamName("Cura\u00E7ao");
        TeamName decomposed = new TeamName("Curac\u0327ao");

        assertThat(decomposed).isEqualTo(composed);
        assertThat(decomposed.display()).isEqualTo("Cura\u00E7ao");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Spain\u200B", "\uFEFFSpain", "Sp\u00ADain", "\u200ESpain\u200D"})
    void removesInvisibleFormatCharacters(String rawName) {
        TeamName team = new TeamName(rawName);

        assertThat(team.display()).isEqualTo("Spain");
    }

    @Test
    void removesFormatCharacterBetweenWords() {
        assertThat(new TeamName("Costa\u200B Rica").display()).isEqualTo("Costa Rica");
    }

    @ParameterizedTest
    @ValueSource(strings = {"\u0000", "Spa\u0000in", "Spain\u0007", "\u007FSpain", "Spain\u0085"})
    void rejectsControlCharacters(String rawName) {
        assertThatThrownBy(() -> new TeamName(rawName))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorAlwaysNormalises() {
        TeamName team = new TeamName("\u200B SPAIN\u00A0 ");

        assertThat(team.display()).isEqualTo("SPAIN");
        assertThat(team.key()).isEqualTo("spain");
    }

    @Test
    void keepsOriginalCaseForDisplay() {
        TeamName team = new TeamName("SPAIN");

        assertThat(team.display()).isEqualTo("SPAIN");
    }

    @Test
    void keyIgnoresCase() {
        assertThat(new TeamName("SPAIN").key()).isEqualTo(new TeamName("spain").key());
    }

    @Test
    void equalIgnoringCase() {
        TeamName spain = new TeamName("Spain");
        TeamName lowerCase = new TeamName("spain");

        assertThat(lowerCase).isEqualTo(spain);
        assertThat(lowerCase.hashCode()).isEqualTo(spain.hashCode());
    }

    @Test
    void equalIgnoringCaseAndWhitespace() {
        assertThat(new TeamName("\u2003sPaIn ")).isEqualTo(new TeamName("Spain"));
    }

    @Test
    void differentTeamsAreNotEqual() {
        assertThat(new TeamName("Spain")).isNotEqualTo(new TeamName("Brazil"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\u2003", "\u3000 \n", "\u00A0", "\u2007\u202F", "\u200B", "\uFEFF \u200D"})
    void rejectsNullAndBlankNames(String rawName) {
        assertThatThrownBy(() -> new TeamName(rawName))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
