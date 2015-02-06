package com.github.amidupeuple.main;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.amidupeuple.model.Song;

/**
 * Created by dpivovar on 18.11.2014.
 */
public class SongAdapter extends BaseAdapter {
    private static final String TAG = "SongAdapter";

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private MainActivity mainActivity;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        mainActivity = (MainActivity) c;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.song, parent, false);
        TextView songView = (TextView) songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
        Song currSong = songs.get(position);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        songLay.setTag(position);
        if (mainActivity.getSelectedItemPosition() == position) {
            songLay.setBackgroundColor(Color.WHITE);
        }

        return songLay;
    }
}
