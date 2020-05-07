package com.airelogic.artistinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MBArtist {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("disambiguation")
    private String disambiguation;

    public MBArtist() {
    }

    public MBArtist(String id, String name, String disambiguation) {
        this.id = id;
        this.name = name;
        this.disambiguation = disambiguation;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisambiguation() {
        return disambiguation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MBArtist mbArtist = (MBArtist) o;
        return Objects.equals(id, mbArtist.id) &&
                Objects.equals(name, mbArtist.name) &&
                Objects.equals(disambiguation, mbArtist.disambiguation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, disambiguation);
    }

    @Override
    public String toString() {
        return "MBArtist{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                '}';
    }
}
