package com.airelogic.artistinfo.controllers;

import com.airelogic.artistinfo.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.averagingInt;

@RestController
@RequestMapping(value = "/api")
public class ArtistController {

    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<String, Integer> songToWordCountCache;

    private static final int MAX_SEARCH_RESULTS = 100;
    private static final int MUSICBRAINZ_MILLIS_BETWEEN_REQUESTS = 1000;

    public ArtistController(RestTemplate restTemplate, ConcurrentHashMap<String, Integer> songToWordCountCache) {
        this.restTemplate = restTemplate;
        this.songToWordCountCache = songToWordCountCache;
    }

    /**
     * Using the Music brainz api to search for the potential artists.
     */
    @GetMapping(value = "/search/{artistName}")
    public List<MBArtist> searchArtistsFor(@PathVariable("artistName") String artistSearchTerm) {

        //Only return the first 100, doubtful the users artist will be beyond that, they can be more specific.
        URI uri = UriComponentsBuilder.fromHttpUrl("https://musicbrainz.org/ws/2/artist")
                .queryParam("fmt", "json")
                .queryParam("limit", MAX_SEARCH_RESULTS)
                .queryParam("query", artistSearchTerm)
                .build()
                .toUri();

        ResponseEntity<MBArtistList> restRes = restTemplate.getForEntity(uri, MBArtistList.class);

        return restRes.getBody().getMbArtistList();
    }

    /**
     * Warning: both the lyrics and music brainz api produce different return values given the same inputs somewhat
     * randomly. This is mitigated slightly by caching the lyrics count however the return value of this method
     * will fluctuate because of this.
     *
     * Don't cache the recordings for an artist as these are likely to change.
     */
    @GetMapping(value = "/lyrics/{artistId}")
    public Double getAvgNumWords(@PathVariable("artistId") String artistId) {

        String urlArtist = "https://musicbrainz.org/ws/2/artist/" + artistId + "?fmt=json";
        MBArtist mbArtist = restTemplate.getForEntity(urlArtist, MBArtist.class).getBody();

        List<MBRecording> allMbRecordings = getMbRecordingsFor(artistId, MAX_SEARCH_RESULTS);

        //Can do this in parallel to speed things up as the lyrics api doesn't appear to have throttling, we are
        //going to our cache most of the time anyway.
        return allMbRecordings.parallelStream()
                .map(MBRecording::getTitle)
                .distinct() //The MB api can return duplicate recordings of the same title.
                .map(title -> findWordCount(mbArtist.getName(), title))
                .filter(Objects::nonNull) //For songs we can't find the lyrics to.
                .collect(averagingInt(Integer::intValue));
    }

    /**
     * Get the word count out of the cache or search it using the api and then cache it. This is fine because the
     * words to a song should never change. Searches the api again if it wasn't found last time as their DB could
     * have been updated. Returns null if the lyrics can't be found in the cache or from the api.
     */
    public Integer findWordCount(String artist, String songTitle) {
        String songKey = artist + " - " + songTitle;
        Integer songWordCount = songToWordCountCache.get(songKey);
        if (songWordCount == null) {
            String urlLyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + songTitle;
            try {
                LLyrics lLyrics = restTemplate.getForEntity(urlLyrics, LLyrics.class).getBody();
                Lyrics lyrics = new Lyrics(lLyrics.getLyrics());
                songWordCount = lyrics.countNumberOfWords();
                songToWordCountCache.put(songKey, songWordCount);
            } catch (HttpClientErrorException e) {
                System.out.println("Skipping recording, couldn't find lyrics at: " + urlLyrics);
            }
        }

        return songWordCount;
    }

    /**
     * Get the list of recordings for a given artist from the Music Brainz api in batches respecting their throttling.
     */
    public List<MBRecording> getMbRecordingsFor(String artistId, int batchSize) {
        int offset = 0;
        List<MBRecording> allMbRecordings = new ArrayList<>();
        List<MBRecording> mbRecordings;

        long timeOfLastCallMillis = -1;

        do {
            URI uri = UriComponentsBuilder.fromHttpUrl("https://musicbrainz.org/ws/2/recording")
                    .queryParam("fmt", "json")
                    .queryParam("query", "arid:" + artistId)
                    .queryParam("limit", batchSize)
                    .queryParam("offset", offset)
                    .build()
                    .toUri();

            //Music brainz limits the number of requests you can make, see https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting
            long millisToWaitBeforeNextCall = timeOfLastCallMillis - System.currentTimeMillis() + MUSICBRAINZ_MILLIS_BETWEEN_REQUESTS;
            if (millisToWaitBeforeNextCall > 0) {
                try {
                    Thread.sleep(millisToWaitBeforeNextCall);
                } catch (InterruptedException ignored) {}
            }

            timeOfLastCallMillis = System.currentTimeMillis();
            MBRecordingList mbRecordingList = restTemplate.getForEntity(uri, MBRecordingList.class).getBody();
            mbRecordings = mbRecordingList.getMbRecordingList();
            allMbRecordings.addAll(mbRecordings);

            offset += batchSize;

        } while (!mbRecordings.isEmpty());

        return allMbRecordings;
    }
}
