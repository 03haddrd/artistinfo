package com.airelogic.artistinfo.controllers;

import com.airelogic.artistinfo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ArtistControllerTest {

    @Mock
    RestTemplate mockTemplate;

    ConcurrentHashMap<String, Integer> mockLyricsCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockLyricsCache = new ConcurrentHashMap<>();
    }

    @Test
    void searchArtistsForTestResults() {
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
                                new MBArtist("QueenId", "Queen", "Author of we will rock you"),
                                new MBArtist("QueenId2", "Queen", "Russian band"),
                                new MBArtist("QueenId3", "Quest for life", "Dutch techno artist"))),
                        HttpStatus.OK));

        //Test
        ArtistController artistController = new ArtistController(mockTemplate, mockLyricsCache);
        List<MBArtist> mbArtists = artistController.searchArtistsFor(artistSearchTerm);

        assertEquals(3, mbArtists.size());

        //Verify
        verify(mockTemplate).getForEntity(uri, MBArtistList.class);
        verifyNoMoreInteractions(mockTemplate);
    }

    @Test
    void searchArtistsForTestNoResults() {
        //Setup
        String artistSearchTerm = "quee";

        MBArtistList mbArtistList = new MBArtistList(Arrays.asList(
        ));

        ResponseEntity<MBArtistList> res = new ResponseEntity<>(mbArtistList, HttpStatus.OK);

        URI uri = UriComponentsBuilder.fromHttpUrl("https://musicbrainz.org/ws/2/artist")
                .queryParam("fmt", "json")
                .queryParam("limit", 100)
                .queryParam("query", artistSearchTerm)
                .build()
                .toUri();
        when(mockTemplate.getForEntity(uri, MBArtistList.class)).thenReturn(res);

        //Test
        ArtistController artistController = new ArtistController(mockTemplate, mockLyricsCache);
        List<MBArtist> mbArtists = artistController.searchArtistsFor(artistSearchTerm);

        assertEquals(0, mbArtists.size());

        //Verify
        verify(mockTemplate).getForEntity(uri, MBArtistList.class);
        verifyNoMoreInteractions(mockTemplate);
    }

    @Test
    void getAvgNumWordsTestAverage() {
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

        String song1 = "Song with 5 words";
        String song2 = "Song with 4 words";
        String song3 = "Song with 8 words";

        String urlSong1Lyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + song1;
        String urlSong2Lyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + song2;
        String urlSong3Lyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + song3;

        //Artist
        when(mockTemplate.getForEntity(urlArtist, MBArtist.class))
                .thenReturn(new ResponseEntity<>(
                        new MBArtist(artistId, artist, "Author of we will rock you"),
                        HttpStatus.OK));

        //Recordings
        when(mockTemplate.getForEntity(uriRecordingOffset0, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList(
                                new MBRecording("RecordingId", song1),
                                new MBRecording("RecordingId", song2),
                                new MBRecording("RecordingId", song3))),
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(uriRecordingOffset100 , MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList()),
                        HttpStatus.OK));

        //Lyrics
        when(mockTemplate.getForEntity(urlSong1Lyrics, LLyrics.class))
                .thenReturn(new ResponseEntity<>(
                        new LLyrics("I'm contemplating thinking about thinking."), //5 words
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(urlSong2Lyrics, LLyrics.class))
                .thenReturn(new ResponseEntity<>(
                        new LLyrics("I love Aire Logic!"), //4 words
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(urlSong3Lyrics, LLyrics.class))
                .thenReturn(new ResponseEntity<>(
                        new LLyrics("Denial is not just a river in Egypt"), //8 words
                        HttpStatus.OK));


        //Test
        ArtistController artistController = new ArtistController(mockTemplate, mockLyricsCache);
        Double averageNumLyrics = artistController.getAvgNumWords(artistId);

        assertEquals(5.66, averageNumLyrics, 0.01);

        //Verify
        verify(mockTemplate).getForEntity(urlArtist, MBArtist.class);
        verify(mockTemplate).getForEntity(uriRecordingOffset0, MBRecordingList.class);
        verify(mockTemplate).getForEntity(uriRecordingOffset100, MBRecordingList.class);
        verify(mockTemplate).getForEntity(urlSong1Lyrics, LLyrics.class);
        verify(mockTemplate).getForEntity(urlSong2Lyrics, LLyrics.class);
        verify(mockTemplate).getForEntity(urlSong3Lyrics, LLyrics.class);
        verifyNoMoreInteractions(mockTemplate);
    }

    @Test
    void getAvgNumWordsTestClientError() {
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

        String song1 = "Song with 5 words";
        String song2 = "Song with that can't be found";
        String song3 = "Song with 8 words";

        String urlSong1Lyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + song1;
        String urlSong2Lyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + song2;
        String urlSong3Lyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + song3;

        //Artist
        when(mockTemplate.getForEntity(urlArtist, MBArtist.class))
                .thenReturn(new ResponseEntity<>(
                        new MBArtist(artistId, artist, "Author of we will rock you"),
                        HttpStatus.OK));

        //Recordings
        when(mockTemplate.getForEntity(uriRecordingOffset0, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList(
                                new MBRecording("RecordingId", song1),
                                new MBRecording("RecordingId", song2),
                                new MBRecording("RecordingId", song3))),
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(uriRecordingOffset100, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList()),
                        HttpStatus.OK));

        //Lyrics
        when(mockTemplate.getForEntity(urlSong1Lyrics, LLyrics.class))
                .thenReturn(new ResponseEntity<>(
                        new LLyrics("I'm contemplating thinking about thinking."), //5 words
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(urlSong2Lyrics, LLyrics.class)).thenThrow(HttpClientErrorException.class); // Song can't be found

        when(mockTemplate.getForEntity(urlSong3Lyrics, LLyrics.class))
                .thenReturn(new ResponseEntity<>(
                        new LLyrics("Denial is not just a river in Egypt"), //8 words
                        HttpStatus.OK));

        //Test
        ArtistController artistController = new ArtistController(mockTemplate, mockLyricsCache);
        double averageNumLyrics = artistController.getAvgNumWords(artistId);

        assertEquals(6.5, averageNumLyrics, 0.01);

        //Verify
        verify(mockTemplate).getForEntity(urlArtist, MBArtist.class);
        verify(mockTemplate).getForEntity(uriRecordingOffset0, MBRecordingList.class);
        verify(mockTemplate).getForEntity(uriRecordingOffset100, MBRecordingList.class);
        verify(mockTemplate).getForEntity(urlSong1Lyrics, LLyrics.class);
        verify(mockTemplate).getForEntity(urlSong2Lyrics, LLyrics.class);
        verify(mockTemplate).getForEntity(urlSong3Lyrics, LLyrics.class);
        verifyNoMoreInteractions(mockTemplate);
    }

    @Test
    void getMbRecordingsForTest() {

        //Setup
        String artistId = "QueenId";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://musicbrainz.org/ws/2/recording")
                .queryParam("fmt", "json")
                .queryParam("query", "arid:" + artistId)
                .queryParam("limit", 2)
                .queryParam("offset", 0);

        URI uriOffset0 = builder.build().toUri();
        URI uriOffset2 = builder.replaceQueryParam("offset", 2).build().toUri();
        URI uriOffset4 = builder.replaceQueryParam("offset", 4).build().toUri();

        when(mockTemplate.getForEntity(uriOffset0, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList(
                                new MBRecording("RecordingId", "song1"),
                                new MBRecording("RecordingId", "song2"))),
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(uriOffset2, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList(
                                new MBRecording("RecordingId", "song3"))),
                        HttpStatus.OK));

        when(mockTemplate.getForEntity(uriOffset4, MBRecordingList.class))
                .thenReturn(new ResponseEntity<>(
                        new MBRecordingList(Arrays.asList()),
                        HttpStatus.OK));

        //Test
        ArtistController artistController = new ArtistController(mockTemplate, mockLyricsCache);
        List<MBRecording> mbRecordings = artistController.getMbRecordingsFor(artistId, 2);

        assertEquals(3, mbRecordings.size());

        //Verify
        verify(mockTemplate).getForEntity(uriOffset0, MBRecordingList.class);
        verify(mockTemplate).getForEntity(uriOffset2, MBRecordingList.class);
        verify(mockTemplate).getForEntity(uriOffset4, MBRecordingList.class);
        verifyNoMoreInteractions(mockTemplate);
    }

    @Test
    void findWordCountTestGoesToCache() {

        //Setup
        String artist = "Artist";
        String songTitle = "SongTitle with 10 words";
        String songKey = artist + " - " + songTitle;

        mockLyricsCache.put(songKey, 10);

        //Test
        ArtistController artistController = new ArtistController(mockTemplate, mockLyricsCache);
        Integer wordCount = artistController.findWordCount(artist, songTitle);

        assertEquals(10, wordCount);

        //Verify
        verifyNoInteractions(mockTemplate);
    }

    @Test
    void findWordCountTestPutsInCache() {

        //Setup
        String artist = "Artist";
        String songTitle = "SongTitle with 5 words";
        String urlSongLyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + songTitle;

        when(mockTemplate.getForEntity(urlSongLyrics, LLyrics.class))
                .thenReturn(new ResponseEntity<>(
                        new LLyrics("I'm contemplating thinking about thinking."), //5 words
                        HttpStatus.OK));

        //Test
        ArtistController artistController = new ArtistController(mockTemplate, mockLyricsCache);
        Integer wordCountFirst = artistController.findWordCount(artist, songTitle);
        Integer wordCountSecond = artistController.findWordCount(artist, songTitle);

        assertEquals(5, wordCountFirst);
        assertEquals(5, wordCountSecond);

        //Verify
        verify(mockTemplate).getForEntity(urlSongLyrics, LLyrics.class); //Check the api was only consulted once.
        verifyNoMoreInteractions(mockTemplate);
    }

}