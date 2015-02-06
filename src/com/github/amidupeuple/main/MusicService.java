package com.github.amidupeuple.main;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.github.amidupeuple.model.Song;

import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

/**
 * Created by dpivovar on 18.11.2014.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private static final String TAG = "MusicService";
    private static final int NOTIFY_ID=1;

    private MediaPlayer player;         //media player
    private ArrayList<Song> songs;      //song list
    private int songPosn;               //current position
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private boolean shuffle=false;
    private Random rand;
    private MediaPlayerState mMediaPlayerCurrState;

    public MediaPlayerState getMediaPlayerCurrState() {
        return mMediaPlayerCurrState;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mMediaPlayerCurrState = MediaPlayerState.STARTED;

        notifyActivityToUpdateSeekBar();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendIntent)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        mMediaPlayerCurrState = MediaPlayerState.IDLE;
        initMusicPlayer();
        rand = new Random();
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setShuffle() {
        if (shuffle) {
            shuffle = false;
        } else {
            shuffle = true;
        }
    }

    public  void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong(){
        player.reset();
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e(TAG, "Error setting data source", e);
        }
        mMediaPlayerCurrState = MediaPlayerState.INITIALIZED;

        Log.d(TAG, "before preparing");
        try {
            player.prepare();
        } catch (IOException e) {
            Log.e(TAG, null, e);
        }
        mMediaPlayerCurrState = MediaPlayerState.PREPARED;
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
        mMediaPlayerCurrState = MediaPlayerState.PAUSED;
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void resumePlayback(){
        player.start();
        mMediaPlayerCurrState = MediaPlayerState.STARTED;
        notifyActivityToUpdateSeekBar();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0) {
            songPosn=songs.size()-1;
        }
        playSong();
    }

    public void playNext(){
        if (shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size());
            }
        } else {
            songPosn++;
            if(songPosn >= songs.size()) {
                songPosn=0;
            }
        }
        playSong();
    }

    public long getSongId() {
        return songs.get(songPosn).getId();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    private void notifyActivityToUpdateSeekBar() {
        Intent intent = new Intent("updateSeekbar");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public enum MediaPlayerState{IDLE, INITIALIZED, PREPARED, STARTED, PAUSED};
}
