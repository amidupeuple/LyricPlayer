package com.github.amidupeuple.main;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.github.amidupeuple.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dpivovar on 01.04.2015.
 */
public class AllSongsListFragment extends Fragment {
    private ListView mSongListView;
    private ArrayList<Song> mSongList;
    private SongAdapter mSongAdapter;

    private static final String TAG = "AllSongsListFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.all_songs_list_fragment, container, false);

        mSongListView = (ListView) v.findViewById(R.id.song_list);

        mSongList = ((MainActivity) getActivity()).getSongList();

        mSongAdapter = new SongAdapter(getActivity(), mSongList);
        mSongListView.setAdapter(mSongAdapter);

        return v;
    }
}
