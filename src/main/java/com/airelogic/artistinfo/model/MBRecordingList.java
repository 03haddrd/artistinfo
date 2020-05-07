package com.airelogic.artistinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class MBRecordingList {

    @JsonProperty("recordings")
    private List<MBRecording> mbRecordingList;

    public MBRecordingList() {
    }

    public MBRecordingList(List<MBRecording> mbRecordingList) {
        this.mbRecordingList = mbRecordingList;
    }

    public List<MBRecording> getMbRecordingList() {
        return mbRecordingList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MBRecordingList that = (MBRecordingList) o;
        return Objects.equals(mbRecordingList, that.mbRecordingList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mbRecordingList);
    }

    @Override
    public String toString() {
        return "MBRecordingList{" +
                "mbRecordingList=" + mbRecordingList +
                '}';
    }
}
