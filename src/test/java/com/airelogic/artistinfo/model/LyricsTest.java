package com.airelogic.artistinfo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LyricsTest {

    @Test
    void countNumberOfWordsSimple() {
        Lyrics lyrics = new Lyrics("One");

        assertEquals(1, lyrics.countNumberOfWords());
    }

    @Test
    void countNumberOfWordsSentence() {
        Lyrics lyrics = new Lyrics("One two three four");

        assertEquals(4, lyrics.countNumberOfWords());
    }

    @Test
    void countNumberOfWordsDoubleSpaces() {
        Lyrics lyrics = new Lyrics("   One      two   ");

        assertEquals(2, lyrics.countNumberOfWords());
    }

    @Test
    void countNumberOfWordsNewLines() {
        Lyrics lyrics = new Lyrics("One\n\n\ntwo");

        assertEquals(2, lyrics.countNumberOfWords());
    }

    @Test
    void countNumberOfWordsMixed() {
        Lyrics lyrics = new Lyrics("  \n\nOne\n\n \n\nTwo  \n");

        assertEquals(2, lyrics.countNumberOfWords());
    }
}