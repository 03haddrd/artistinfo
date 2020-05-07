package com.airelogic.artistinfo.controllers;

import com.airelogic.artistinfo.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class ArtistControllerIntegrationTest {

    @MockBean
    private RestTemplate mockTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchArtistsForTest() throws Exception {
        //Setup
        String artistSearchTerm = "que";
        URI uri = UriComponentsBuilder.fromHttpUrl("https://musicbrainz.org/ws/2/artist")
                .queryParam("fmt", "json")
                .queryParam("limit", 100)
                .queryParam("query", artistSearchTerm)
                .build()
                .toUri();

        when(mockTemplate.getForEntity(uri, MBArtistList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBArtistList(Arrays.asList(
                                new MBArtist("QueenId", "Quest for life", "Dutch techno artist"))),
                        HttpStatus.OK));

        //Test
        mockMvc.perform(get("/api/search/" + artistSearchTerm))

                //Response
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                //Contents
                .andExpect(jsonPath("$[0].id", is("QueenId")))
                .andExpect(jsonPath("$[0].name", is("Quest for life")))
                .andExpect(jsonPath("$[0].disambiguation", is("Dutch techno artist")));
    }

    @Test
    void getAvgNumWordsTest() throws Exception {
        //Setup
        String artist = "Queen";
        String artistId = "QueenId";
        String urlArtist = "https://musicbrainz.org/ws/2/artist/" + artistId + "?fmt=json";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://musicbrainz.org/ws/2/recording")
                .queryParam("fmt", "json")
                .queryParam("query", "arid:" + artistId)
                .queryParam("limit", 100)
                .queryParam("offset", 0);

        URI uriRecordingOffset0 = builder.build().toUri();
        URI uriRecordingOffset100 = builder.replaceQueryParam("offset", 100).build().toUri();

        String song = "Song with 5 words";

        String urlSongLyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + song;

        //Artist
        when(mockTemplate.getForEntity(urlArtist, MBArtist.class))
                .thenReturn(new ResponseEntity<>(
                        new MBArtist(artistId, artist, "Author of we will rock you"),
                        HttpStatus.OK));

        //Recordings
        when(mockTemplate.getForEntity(uriRecordingOffset0, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList(
                                new MBRecording("RecordingId", song))),
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(uriRecordingOffset100, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList()),
                        HttpStatus.OK));

        //Lyrics
        when(mockTemplate.getForEntity(urlSongLyrics, LLyrics.class))
                .thenReturn(new ResponseEntity<>(
                        new LLyrics("I'm contemplating thinking about thinking."), //5 words
                        HttpStatus.OK));

        //Test
        mockMvc.perform(get("/api/lyrics/" + artistId))

                //Response
                .andExpect(status().isOk())

                //Contents
                .andExpect(jsonPath("$", closeTo(5, 0.01)));
    }
}