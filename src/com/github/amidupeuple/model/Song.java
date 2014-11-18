package com.github.amidupeuple.model;

/**
 * Created by dpivovar on 18.11.2014.
 */
public class Song {
    private long id;
    private String title;
    private String artist;

    public Song(long songID, String songTitle, String songArtist) {
        id = songID;
        title = songTitle;
        artist = songArtist;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
