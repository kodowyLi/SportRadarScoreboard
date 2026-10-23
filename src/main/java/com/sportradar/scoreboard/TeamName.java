package com.sportradar.scoreboard;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

// The only place where team names are validated and normalised. Normalisation happens in the
// constructor, so no TeamName can exist with an unvalidated or unnormalised name.
record TeamName(String display) {

    // Invisible characters such as zero-width spaces, soft hyphens and byte order marks.
    private static final Pattern FORMAT_CHARACTERS = Pattern.compile("\\p{Cf}");
    // \s alone misses no-break spaces (U+00A0, U+2007, U+202F); \p{Z} covers them.
    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\p{Z}]+");
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cc}");

    TeamName {
        String visible = FORMAT_CHARACTERS.matcher(display == null ? "" : display).replaceAll("");
        // Runs before the control character check, which would otherwise reject tabs and newlines.
        String singleSpaced = WHITESPACE.matcher(visible).replaceAll(" ").strip();
        if (CONTROL_CHARACTERS.matcher(singleSpaced).find()) {
            throw new IllegalArgumentException("Team name must not contain control characters");
        }
        // NFC, so that a precomposed c-cedilla and "c" followed by a combining cedilla are equal.
        display = Normalizer.normalize(singleSpaced, Normalizer.Form.NFC);
        if (display.isEmpty()) {
            throw new IllegalArgumentException("Team name must not be null or blank");
        }
    }

    // Case-insensitive identity of the team, used for lookups.
    String key() {
        return display.toLowerCase(Locale.ROOT);
    }

    // Two names are the same team when their keys match, e.g. "Spain" and "spain".
    @Override
    public boolean equals(Object other) {
        return other instanceof TeamName team && key().equals(team.key());
    }

    @Override
    public int hashCode() {
        return key().hashCode();
    }
}
