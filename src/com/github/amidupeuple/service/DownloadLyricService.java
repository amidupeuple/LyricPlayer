package com.github.amidupeuple.service;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dpivovar on 05.12.2014.
 */
public class DownloadLyricService {
    private String mArtist;
    private String mSong;
    private String mMockLyric;

    //private static final String ENDPOINT = "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect";
    private static final String ENDPOINT = "http://www.azlyrics.com/lyrics/";
    private static final String ARTIST_PARAM = "artist";
    private static final String SONG_PARAM = "song";

    private static final String TAG = "DownloadLyricService";


    public DownloadLyricService(String artist, String song, String mockLyric) {
        mArtist = artist;
        mSong = song;
        mMockLyric = mockLyric;
    }

    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();   //open connection

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String downloadLyric() {
        String justLyric = null;
        try {
            /*String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter(ARTIST_PARAM, mArtist)
                    .appendQueryParameter(SONG_PARAM, mSong)
                    .build().toString();*/
            //Set url to download lyric
            String url = ENDPOINT +
                         mArtist.toLowerCase().replace(" ", "") +
                         "/" +
                         mSong.toLowerCase().replace(" ", "") +
                         ".html";
            Log.i(TAG, "Link to download: " + url);

            //Get lyric
            String xmlString = mMockLyric;
            //String xmlString = getXml(url);

            //Parse html document to extract only lyric
            /*int startOfLyric = xmlString.indexOf("<!-- start of lyrics -->") + 24;
            int endOfLyric = xmlString.indexOf("<!-- end of lyrics -->") - 1;
            justLyric = xmlString.substring(startOfLyric, endOfLyric);*/
            justLyric = xmlString;
            Log.i(TAG, "Received lyric: \n" + justLyric);
        } catch (Exception ioe ) {
            Log.e(TAG, "Failed to download lyric", ioe);
        }

        return justLyric;
    }

    private String getXml(String url) throws IOException {
        return new String(getUrlBytes(url));
    }
}
