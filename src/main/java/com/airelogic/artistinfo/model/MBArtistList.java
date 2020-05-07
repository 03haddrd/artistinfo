package com.airelogic.artistinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class MBArtistList {

    @JsonProperty("artists")
    private List<MBArtist> mbArtistList;

    public MBArtistList() {
    }

    public MBArtistList(List<MBArtist> mbArtistList) {
        this.mbArtistList = mbArtistList;
    }

    public List<MBArtist> getMbArtistList() {
        return mbArtistList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MBArtistList that = (MBArtistList) o;
        return Objects.equals(mbArtistList, that.mbArtistList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mbArtistList);
    }

    @Override
    public String toString() {
        return "MBArtistList{" +
                "mbArtistList=" + mbArtistList +
                '}';
    }
}
