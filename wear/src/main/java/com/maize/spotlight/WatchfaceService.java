package com.maize.spotlight;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.SystemProviders;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Will Hou (will@ezi.am)
 * @date April 19, 2015
 */
public class WatchfaceService extends CanvasWatchFaceService {

    public static final int LEFT_COMPLICATION_ID = 300;
    public static final int RIGHT_COMPLICATION_ID = 301;
    private static final int[] COMPLICATION_IDS = {LEFT_COMPLICATION_ID, RIGHT_COMPLICATION_ID};

    @Override
    public Engine onCreateEngine() {
        return new WatchfaceEngine();
    }

    /* implement service callback methods */
    private class WatchfaceEngine extends CanvasWatchFaceService.Engine {

        private static final int HALF_DAY_IN_MIN = 720;
        private static final int ONE_HOUR_IN_MIN = 60;
        private static final float MIN_PER_DEGREE = HALF_DAY_IN_MIN / 360f;
        private static final float CENTER_OFFSET = 0.75f;
        private static final float CLOCKFACE_RADIUS = 0.93f;

        private static final float HOUR_INDICATOR_SIZE_FACTOR = 0.1f;
        private static final float HOUR_TEXT_SIZE_FACTOR = 0.2f;
        private static final float HALF_HOUR_INDICATOR_SIZE_FACTOR = 0.05f;
        private static final float TEN_MIN_INDICATOR_SIZE_FACTOR = 0.025f;

        private Paint mLinePaint;
        private Paint mHourPaint;
        private Paint mTenMinutePaint;
        private Paint mHourTextPaint;
        private Paint mComplicationTextPaint;

        private int mBackgroundColor;
        private int mAccentColor;

        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMute;
        private Calendar mCalendar;

        private SparseArray<ComplicationData> mComplicationDataArray;
        private SparseArray<ComplicationDrawable> mComplicationDrawableArray;
        private final Rect mLeftBounds = new Rect();
        private final Rect mRightBounds = new Rect();

        private final SimpleDateFormat mMonthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        private final SimpleDateFormat mDayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        private final SimpleDateFormat mDayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String timeZone = intent.getStringExtra("time-zone");
                if (timeZone != null) {
                    mCalendar.setTimeZone(TimeZone.getTimeZone(timeZone));
                }
                mCalendar.setTimeInMillis(System.currentTimeMillis());
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mMute = getInterruptionFilter() == INTERRUPTION_FILTER_NONE;
            updateWatchFaceStyle();

            /* initialize your watch face */
            mBackgroundColor = Color.BLACK;
            mAccentColor = 0xAAFF6600;

            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setAntiAlias(true);
            mLinePaint.setColor(mAccentColor);
            mLinePaint.setStyle(Style.STROKE);
            mLinePaint.setStrokeWidth(3f);

            mHourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setColor(Color.WHITE);
            mHourPaint.setStyle(Style.STROKE);
            mHourPaint.setStrokeWidth(3f);

            mTenMinutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTenMinutePaint.setAntiAlias(true);
            mTenMinutePaint.setColor(Color.WHITE);
            mTenMinutePaint.setStyle(Style.STROKE);
            mTenMinutePaint.setStrokeWidth(2f);

            DisplayMetrics metrics = getResources().getDisplayMetrics();

            mHourTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHourTextPaint.setAntiAlias(true);
            mHourTextPaint.setColor(Color.WHITE);
            mHourTextPaint.setStyle(Style.FILL);
            mHourTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, metrics));
            mHourTextPaint.setTextAlign(Paint.Align.CENTER);

            mComplicationTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mComplicationTextPaint.setTextAlign(Paint.Align.CENTER);

            mCalendar = Calendar.getInstance();

            initComplications();
        }

        private void initComplications() {
            mComplicationDataArray = new SparseArray<>(COMPLICATION_IDS.length);
            mComplicationDrawableArray = new SparseArray<>(COMPLICATION_IDS.length);

            ComplicationDrawable leftDrawable = new ComplicationDrawable(WatchfaceService.this);
            ComplicationDrawable rightDrawable = new ComplicationDrawable(WatchfaceService.this);
            initComplicationDrawable(leftDrawable);
            initComplicationDrawable(rightDrawable);

            mComplicationDrawableArray.put(LEFT_COMPLICATION_ID, leftDrawable);
            mComplicationDrawableArray.put(RIGHT_COMPLICATION_ID, rightDrawable);

            setActiveComplications(COMPLICATION_IDS);
        }

        private void initComplicationDrawable(ComplicationDrawable drawable) {
            drawable.setTextColorActive(0xCCFFFFFF);
            drawable.setTitleColorActive(0x88FFFFFF);
            drawable.setTextTypefaceActive(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            drawable.setTitleTypefaceActive(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
            drawable.setBackgroundColorActive(Color.TRANSPARENT);
            drawable.setBorderColorActive(Color.TRANSPARENT);
            drawable.setTextColorAmbient(0x77FFFFFF);
            drawable.setTitleColorAmbient(0x55FFFFFF);
            drawable.setBackgroundColorAmbient(Color.TRANSPARENT);
            drawable.setBorderColorAmbient(Color.TRANSPARENT);
            drawable.setHighlightColorActive(mAccentColor);
            drawable.setRangedValuePrimaryColorActive(mAccentColor);
            drawable.setRangedValueSecondaryColorActive(0x33FFFFFF);
        }

        @Override
        public void onComplicationDataUpdate(int complicationId, ComplicationData complicationData) {
            super.onComplicationDataUpdate(complicationId, complicationData);
            mComplicationDataArray.put(complicationId, complicationData);
            ComplicationDrawable drawable = mComplicationDrawableArray.get(complicationId);
            if (drawable != null) {
                drawable.setComplicationData(complicationData);
                invalidate();
            }
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            if (tapType == TAP_TYPE_TAP) {
                for (int compId : COMPLICATION_IDS) {
                    Rect bounds = (compId == LEFT_COMPLICATION_ID) ? mLeftBounds : mRightBounds;
                    if (bounds.contains(x, y)) {
                        ComplicationData data = mComplicationDataArray.get(compId);
                        if (data != null && data.getTapAction() != null) {
                            try {
                                data.getTapAction().send();
                            } catch (PendingIntent.CanceledException ignored) {
                            }
                        } else {
                            launchCalendarApp();
                        }
                        break;
                    }
                }
            }
        }

        private void launchCalendarApp() {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(android.provider.CalendarContract.CONTENT_URI);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            boolean lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            for (int compId : COMPLICATION_IDS) {
                ComplicationDrawable drawable = mComplicationDrawableArray.get(compId);
                if (drawable != null) {
                    drawable.setLowBitAmbient(lowBitAmbient);
                    drawable.setBurnInProtection(burnInProtection);
                }
            }
            invalidate();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            for (int compId : COMPLICATION_IDS) {
                ComplicationDrawable drawable = mComplicationDrawableArray.get(compId);
                if (drawable != null) {
                    drawable.setInAmbientMode(inAmbientMode);
                }
            }
            invalidate();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE;
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                updateWatchFaceStyle();
                invalidate();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                mCalendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                unregisterReceiver();
            }

            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Update the time
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            int hour = mCalendar.get(Calendar.HOUR);
            int minute = mCalendar.get(Calendar.MINUTE);

            final int totalMinutes = hour * ONE_HOUR_IN_MIN + minute;
            final float degrees = totalMinutes / MIN_PER_DEGREE - 90f;

            // Draw background color
            canvas.drawColor(mBackgroundColor);

            Path path = new Path();

            int width = bounds.width();
            int height = bounds.height();
            int size = Math.min(width, height);

            int viewCenterX = width / 2;
            int viewCenterY = height / 2;

            float centerX, centerY;

            if ((hour == 0 || hour == 12) && minute == 0) {
                centerX = viewCenterX;
                centerY = viewCenterY + size * CENTER_OFFSET;
            } else if (hour == 6 && minute == 0) {
                centerX = viewCenterX;
                centerY = viewCenterY - size * CENTER_OFFSET;
            } else {
                boolean beforeSix = hour < 6;
                centerX = beforeSix ?
                        viewCenterX + (float) (size * CENTER_OFFSET * Math.cos(Math.toRadians(180 - degrees))) :
                        viewCenterX + (float) (size * CENTER_OFFSET * Math.cos(Math.toRadians(degrees - 180)));
                centerY = beforeSix ?
                        viewCenterY - (float) (size * CENTER_OFFSET * Math.sin(Math.toRadians(180 - degrees))) :
                        viewCenterY + (float) (size * CENTER_OFFSET * Math.sin(Math.toRadians(degrees - 180)));
            }

            //Draw clock face
            for (int i = 0; i < 12; i++) {
                float rotation = i * 30;
                // Draw hour indicators
                path.reset();
                path.moveTo(centerX, centerY - size * CLOCKFACE_RADIUS);
                path.lineTo(centerX, centerY - size * (CLOCKFACE_RADIUS - HOUR_INDICATOR_SIZE_FACTOR));
                canvas.save();
                canvas.rotate(rotation, centerX, centerY);
                canvas.drawPath(path, mHourPaint);

                // Draw hour text
                float textCenterX = centerX;
                float textCenterY = centerY - size * (CLOCKFACE_RADIUS - HOUR_TEXT_SIZE_FACTOR);
                canvas.save();
                canvas.rotate(-rotation, centerX, textCenterY);
                canvas.drawText(
                        String.valueOf(i > 0 ? i : 12),
                        textCenterX,
                        textCenterY + 12,
                        mHourTextPaint);
                canvas.restore();

                // Draw half-hour indicators
                path.reset();
                path.moveTo(centerX, centerY - size * CLOCKFACE_RADIUS);
                path.lineTo(centerX, centerY - size * (CLOCKFACE_RADIUS - HALF_HOUR_INDICATOR_SIZE_FACTOR));
                canvas.rotate(15, centerX, centerY);
                canvas.drawPath(path, mHourPaint);
                canvas.restore();
            }

            for (int i = 0; i < 24; i++) {
                // Draw 10min indicators
                path.reset();
                path.moveTo(centerX, centerY - size * CLOCKFACE_RADIUS);
                path.lineTo(centerX, centerY - size * (CLOCKFACE_RADIUS - TEN_MIN_INDICATOR_SIZE_FACTOR));
                canvas.save();
                canvas.rotate(i * 15 + 5, centerX, centerY);
                canvas.drawPath(path, mTenMinutePaint);
                canvas.rotate(5, centerX, centerY);
                canvas.drawPath(path, mTenMinutePaint);
                canvas.restore();
            }

            // Draw dynamically positioned complications in open space
            drawComplications(canvas, viewCenterX, viewCenterY, size, degrees);

            // Draw red minute "needle"
            path.reset();
            path.moveTo(-width, viewCenterY);
            path.lineTo(width * 2, viewCenterY);
            canvas.save();
            canvas.rotate(degrees, viewCenterX, viewCenterY);
            canvas.drawPath(path, mLinePaint);
            canvas.restore();
        }

        private void drawComplications(Canvas canvas, int centerX, int centerY, int size, float degrees) {
            int compWidth = (int) (size * 0.28f);
            int compHeight = (int) (size * 0.13f);

            float rad = (float) Math.toRadians(degrees);
            float ux = (float) Math.cos(rad);
            float uy = (float) Math.sin(rad);
            float vx = -uy;
            float vy = ux;

            // Dynamically position complications in the open space opposite the needle pointer
            int leftCenterX = (int) (centerX - ux * 0.23f * size - vx * 0.19f * size);
            int leftCenterY = (int) (centerY - uy * 0.23f * size - vy * 0.19f * size);
            mLeftBounds.set(leftCenterX - compWidth / 2, leftCenterY - compHeight / 2, leftCenterX + compWidth / 2, leftCenterY + compHeight / 2);

            int rightCenterX = (int) (centerX - ux * 0.23f * size + vx * 0.19f * size);
            int rightCenterY = (int) (centerY - uy * 0.23f * size + vy * 0.19f * size);
            mRightBounds.set(rightCenterX - compWidth / 2, rightCenterY - compHeight / 2, rightCenterX + compWidth / 2, rightCenterY + compHeight / 2);

            long currentTime = System.currentTimeMillis();

            // Left complication: Default is "Nov 02"
            ComplicationData leftData = mComplicationDataArray.get(LEFT_COMPLICATION_ID);
            ComplicationDrawable leftDrawable = mComplicationDrawableArray.get(LEFT_COMPLICATION_ID);
            if (leftData != null && leftData.getType() == ComplicationData.TYPE_EMPTY) {
                // User explicitly set left slot to empty
            } else if (leftData != null && leftData.getType() != ComplicationData.TYPE_NOT_CONFIGURED) {
                leftDrawable.setBounds(mLeftBounds);
                leftDrawable.draw(canvas, currentTime);
            } else {
                drawDateComplication(canvas, mLeftBounds, size, isInAmbientMode());
            }

            // Right complication: Default is Day of the week ("Mon", "Fri")
            ComplicationData rightData = mComplicationDataArray.get(RIGHT_COMPLICATION_ID);
            ComplicationDrawable rightDrawable = mComplicationDrawableArray.get(RIGHT_COMPLICATION_ID);
            if (rightData != null && rightData.getType() == ComplicationData.TYPE_EMPTY) {
                // User explicitly set right slot to empty
            } else if (rightData != null && rightData.getType() != ComplicationData.TYPE_NOT_CONFIGURED) {
                rightDrawable.setBounds(mRightBounds);
                rightDrawable.draw(canvas, currentTime);
            } else {
                drawDayOfWeekComplication(canvas, mRightBounds, size, isInAmbientMode());
            }
        }

        private void drawDateComplication(Canvas canvas, Rect bounds, int size, boolean inAmbient) {
            String month = mMonthFormat.format(mCalendar.getTime());
            String day = mDayFormat.format(mCalendar.getTime());
            String text = month + " " + day; // e.g. "Nov 02"
            drawUnobtrusiveText(canvas, bounds, text, size, inAmbient);
        }

        private void drawDayOfWeekComplication(Canvas canvas, Rect bounds, int size, boolean inAmbient) {
            String text = mDayOfWeekFormat.format(mCalendar.getTime()); // e.g. "Mon"
            drawUnobtrusiveText(canvas, bounds, text, size, inAmbient);
        }

        private void drawUnobtrusiveText(Canvas canvas, Rect bounds, String text, int size, boolean inAmbient) {
            float textSize = size * 0.046f;
            mComplicationTextPaint.setTextSize(textSize);

            if (inAmbient) {
                mComplicationTextPaint.setColor(0x77FFFFFF);
                mComplicationTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            } else {
                mComplicationTextPaint.setColor(0xCCFFFFFF); // 80% opacity soft white: unobtrusive and sleek
                mComplicationTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            }

            Paint.FontMetrics fm = mComplicationTextPaint.getFontMetrics();
            float x = bounds.centerX();
            float y = bounds.centerY() - (fm.descent + fm.ascent) / 2f;

            canvas.drawText(text, x, y, mComplicationTextPaint);
        }

        private void updateWatchFaceStyle() {
            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchfaceService.this)
                    .setStatusBarGravity(Gravity.TOP | Gravity.CENTER)
                    .setShowUnreadCountIndicator(mMute)
                    .setAcceptsTapEvents(true)
                    .build());
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            ContextCompat.registerReceiver(WatchfaceService.this, mTimeZoneReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchfaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }
    }
}
