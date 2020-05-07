package com.airelogic.artistinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class LLyrics {

    @JsonProperty("lyrics")
    String lyrics;

    public LLyrics() {
    }

    public LLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getLyrics() {
        return lyrics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LLyrics lLyrics = (LLyrics) o;
        return Objects.equals(lyrics, lLyrics.lyrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lyrics);
    }

    @Override
    public String toString() {
        return "LLyrics{" +
                "lyrics='" + lyrics + '\'' +
                '}';
    }
}
