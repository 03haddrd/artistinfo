package com.airelogic.artistinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MBRecording {

    //Don't really need this, adding it in to aid with debugging if necessary and in case the api returns duplicate
    // entries they are considered equal.
    @JsonProperty("id")
    String id;

    @JsonProperty("title")
    String title;

    public MBRecording() {
    }

    public MBRecording(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MBRecording that = (MBRecording) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "MBRecording{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
