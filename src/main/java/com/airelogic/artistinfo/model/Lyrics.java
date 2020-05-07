package com.airelogic.artistinfo.model;

import java.util.Objects;
import java.util.StringTokenizer;

public class Lyrics {

    private final String lyrics;

    public Lyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getLyrics() {
        return lyrics;
    }

    public int countNumberOfWords() {
        StringTokenizer words = new StringTokenizer(lyrics);
        return words.countTokens();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lyrics lyrics1 = (Lyrics) o;
        return Objects.equals(lyrics, lyrics1.lyrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lyrics);
    }

    @Override
    public String toString() {
        return "Lyrics{" +
                "lyrics='" + lyrics + '\'' +
                '}';
    }


}
