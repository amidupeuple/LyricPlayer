package com.github.amidupeuple.main;

import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.*;
import com.github.amidupeuple.model.Song;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import com.github.amidupeuple.service.DownloadLyricService;

public class MainActivity extends FragmentActivity implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "MainActivity";
    private static final int INIT_POSITION = -1;
    public static final String EXTRA_LYRIC = "lyric";

    private SeekBar mSeekBar;
    private ImageButton mPlayPauseButton;
    private boolean isPlayPauseButtonPressed;

    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private SongAdapter mSongAdapter;
    private String mCurrentLyric;

    private int selectedItemPosition = INIT_POSITION;
    private String mockLyric;

    public ArrayList<Song> getSongList() {
        return songList;
    }

    private BroadcastReceiver mUpdateSeekBarReceiver = new BroadcastReceiver() {
        private static final String TAG = "BroadcastReceiver/UpdateSeekBar";

        @Override
        public void onReceive(Context context, Intent intent) {
            mSeekBar.setEnabled(true);
            Log.d(TAG, "duration:" + musicSrv.getDur());
            mSeekBar.setMax(musicSrv.getDur());
            new Thread(new UpdateSeekBar()).start();
            Log.d(TAG, "seek bar update is started!");
        }
    };

    public String getMockLyric() {
        return mockLyric;
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new AllSongsListFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
            Log.d(TAG, "transaction was committed");
        }

        isPlayPauseButtonPressed = false;

        mSeekBar = (SeekBar) findViewById(R.id.musicSeekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setEnabled(false);

        mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlayPauseButtonPressed) {
                    mPlayPauseButton.setImageResource(R.drawable.play);
                    isPlayPauseButtonPressed = false;
                } else {
                    mPlayPauseButton.setImageResource(R.drawable.pause);
                    isPlayPauseButtonPressed = true;
                }
            }
        });

        //Register activity as receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateSeekBarReceiver, new IntentFilter("updateSeekbar"));


        /*songView = (ListView) findViewById(R.id.song_list);
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == selectedItemPosition) {
                    //current item was selected
                    switch(musicSrv.getMediaPlayerCurrState()) {
                        case STARTED: Log.d(TAG, "pause playback");
                            musicSrv.pausePlayer();
                            break;
                        case PAUSED:  Log.d(TAG, "resume playback");
                            musicSrv.resumePlayback();
                            break;
                    }
                } else {
                    //another item was selected
                    mSeekBar.setEnabled(false);

                    int prevPosition = selectedItemPosition;
                    selectedItemPosition = position;
                    songPicked(view);
                    View prevView = songView.getChildAt(prevPosition - songView.getFirstVisiblePosition());
                    if (prevPosition != INIT_POSITION && prevView != null) {
                        prevView.setBackgroundResource(R.color.default_color);
                    }
                    view.setBackgroundColor(Color.WHITE);

                    //Get lyric
                    String artist = ((TextView) view.findViewById(R.id.song_artist)).getText().toString();
                    String song = ((TextView) view.findViewById(R.id.song_title)).getText().toString();
                    new DownloadLyricTask().execute(artist, song, mockLyric);
                }

            }
        });*/
        songList = new ArrayList<Song>();
        initialieSongList();

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        /*mSongAdapter = new SongAdapter(this, songList);
        songView.setAdapter(mSongAdapter);

        //init lyric mock
        initMockLyric();*/
    }

    private void initMockLyric() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.mock_lyric);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                mockLyric = stringBuilder.toString();
                Log.i(TAG, "Mock lyric: \n" + mockLyric);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Error initialized mock lyric", ioe);
        }
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    private void initialieSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
                Log.d(TAG, "New song added: " + thisArtist + ": " + thisAlbum + ": " + thisTitle);
            } while (musicCursor.moveToNext());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            //startService(playIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void songPicked(View view) {
        musicSrv.setSong((Integer.parseInt(view.getTag().toString())));
        musicSrv.playSong();
    }

    private void changeSongListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = new ExpandableListMainFragment();
        fm.beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
        Log.d(TAG, "transaction was committed");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.song_layout:
                changeSongListFragment();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv = null;
                System.exit(0);
                break;
            case R.id.lyric_item:
                Intent i = new Intent(this, LyricActivity.class);
                Bundle b = new Bundle();
                b.putString(EXTRA_LYRIC, mCurrentLyric);
                i.putExtras(b);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class DownloadLyricTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return new DownloadLyricService(params[0], params[1], params[2]).downloadLyric();
        }

        @Override
        protected void onPostExecute(String songLyric) {
            mCurrentLyric = songLyric;
        }
    }

    /**
     * This job starts when
     */
    class UpdateSeekBar implements Runnable {
        private static final String TAG = "UpdateSeekBar";
        @Override
        public void run() {
            Log.d(TAG, "start update seek bar");
            int currPos = musicSrv.getPosn();
            int total = musicSrv.getDur();
            long id = musicSrv.getSongId();

            while (currPos < total && id == musicSrv.getSongId() && musicSrv.isPng()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e(TAG, null, e);
                }
                currPos = musicSrv.getPosn();
                Log.d(TAG, "currPos:" + currPos + " total:" + total);
                mSeekBar.setProgress(currPos);
            }
            Log.d(TAG, "finish update seek bar:currPos/total " + currPos + "/" + total +
                       " initId/curId " + id + "/" + musicSrv.getSongId() + " isPlaying:" + musicSrv.isPng());
        }
    }
}
