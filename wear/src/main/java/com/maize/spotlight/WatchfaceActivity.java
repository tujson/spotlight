package com.maize.spotlight;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.maize.watchface.spotlight.WatchfaceView;

/**
 * @author Will Hou (will@ezi.am)
 * @date Sept 9, 2014
 */
public class WatchfaceActivity extends Activity {

    private static final String TAG = WatchfaceActivity.class.getSimpleName();
    private static final IntentFilter INTENT_FILTER_TIME;

    static {
        INTENT_FILTER_TIME = new IntentFilter();
        INTENT_FILTER_TIME.addAction(Intent.ACTION_TIME_TICK);
        INTENT_FILTER_TIME.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        INTENT_FILTER_TIME.addAction(Intent.ACTION_TIME_CHANGED);
    }

    private WatchfaceView mWatchfaceView;

    public final BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (mWatchfaceView != null) {
                mWatchfaceView.invalidate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWatchfaceView = new WatchfaceView(this);
        setContentView(mWatchfaceView);

        ContextCompat.registerReceiver(this, mTimeInfoReceiver, INTENT_FILTER_TIME, ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWatchfaceView != null) {
            mWatchfaceView.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mTimeInfoReceiver);
        } catch (Exception ignored) {
        }
    }
}
