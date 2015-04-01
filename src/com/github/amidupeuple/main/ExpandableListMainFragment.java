package com.github.amidupeuple.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.github.amidupeuple.model.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dpivovar on 01.04.2015.
 */
public class ExpandableListMainFragment extends Fragment {
    private List<String> mArtistNames = new ArrayList<String>();
    private Map<String, List<String>> mArtistToAlbum = new HashMap<String, List<String>>();
    private ExpandableListAdapterForArtists mAdapterForArtists;
    private ExpandableListView expSongsList;

    private static final String TAG = "ExpandableListMainFragment";


    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Song> allSongs = ((MainActivity) getActivity()).getSongList();
        for (Song song: allSongs) {
            String artist = song.getArtist();
            if (!mArtistNames.contains(artist)) {
                mArtistNames.add(artist);
            }
        }

        for (String artist: mArtistNames) {
            List<String> curArtistAlbums = new ArrayList<String>();
            for (Song song: allSongs) {
                if (song.getArtist().equals(artist)) {
                    if (!curArtistAlbums.contains(song.getAlbum())) {
                        curArtistAlbums.add(song.getAlbum());
                    }
                }
            }
            mArtistToAlbum.put(artist, curArtistAlbums);
        }

        Log.d(TAG, "required collections are initialized");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);

        View v = inflater.inflate(R.layout.artist_album_song_layout, parent, false);
        expSongsList = (ExpandableListView) v.findViewById(R.id.artistAlbumSongExp);
        mAdapterForArtists = new ExpandableListAdapterForArtists(getActivity(), mArtistToAlbum, mArtistNames);
        expSongsList.setAdapter(mAdapterForArtists);

        return v;
    }
}
