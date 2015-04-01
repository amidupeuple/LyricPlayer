package com.github.amidupeuple.main;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by dpivovar on 01.04.2015.
 */
public class ExpandableListAdapterForArtists extends BaseExpandableListAdapter {
    private List<String> mArtistNames;
    private Map<String, List<String>> mArtistToAlbum;
    private Context mContext;

    public ExpandableListAdapterForArtists(Context context,
                                           Map<String, List<String>> artistToAlbum,
                                           List<String> artistNames) {
        mContext = context;
        mArtistNames = artistNames;
        mArtistToAlbum = artistToAlbum;
    }

    @Override
    public int getGroupCount() {
        return mArtistNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mArtistToAlbum.get(mArtistNames.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mArtistNames.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mArtistToAlbum.get(mArtistNames.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String artistName = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.artist_item, null);
        }

        TextView artistHeader = (TextView) convertView.findViewById(R.id.artistHeader);
        artistHeader.setTypeface(null, Typeface.BOLD);
        artistHeader.setText(artistName);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.album_item, null);
        }

        TextView albumHeader = (TextView) convertView.findViewById(R.id.albumHeader);
        albumHeader.setText(childText);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
