package com.github.amidupeuple.main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by dpivovar on 06.12.2014.
 */
public class LyricActivity extends Activity {
    private static final String TAG = "LyricActivity";
    private String mLyric;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyric_activity);

        //Get lyric text
        Bundle b = getIntent().getExtras();
        Log.i(TAG, "bundle == null: " + (b == null));
        if (b != null) {
            mLyric = b.getString(MainActivity.EXTRA_LYRIC);
        }

        //Set lyric in text view
        TextView lyricTextView = (TextView) findViewById(R.id.lyric_textView);
        Log.i(TAG, "Lyric: \n" + mLyric);
        lyricTextView.setText(mLyric);
    }

}
