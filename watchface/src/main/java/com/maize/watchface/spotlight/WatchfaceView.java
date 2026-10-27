package com.maize.watchface.spotlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Will Hou (will@ezi.am)
 * @date Sept 11, 2014
 */
public class WatchfaceView extends View {

    private static final int HALF_DAY_IN_MIN = 720;
    private static final int ONE_HOUR_IN_MIN = 60;
    private static final float MIN_PER_DEGREE = HALF_DAY_IN_MIN / 360f;
    private static final float CENTER_OFFSET = 0.75f;
    private static final float CLOCKFACE_RADIUS = 0.93f;

    private static final float HOUR_INDICATOR_SIZE_FACTOR = 0.1f;
    private static final float HOUR_TEXT_SIZE_FACTOR = 0.2f;
    private static final float HALF_HOUR_INDICATOR_SIZE_FACTOR = 0.05f;
    private static final float TEN_MIN_INDICATOR_SIZE_FACTOR = 0.025f;

    public static final String KEY_BACKGROUND = WatchfaceView.class.getSimpleName() + ".KEY_BACKGROUND";
    public static final String KEY_ACCENT = WatchfaceView.class.getSimpleName() + ".KEY_ACCENT";

    private Paint mLinePaint;
    private Paint mHourPaint;
    private Paint mTenMinutePaint;
    private Paint mHourTextPaint;
    private Paint mComplicationTextPaint;
    private Paint mComplicationTitlePaint;
    private Paint mComplicationBoxPaint;
    private Paint mComplicationBgPaint;

    private int mBackgroundColor;
    private int mAccentColor;
    private boolean mShowComplications = true;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE d", Locale.getDefault());
    private final SimpleDateFormat mMonthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

    public WatchfaceView(Context context) {
        super(context);
        init();
    }

    public WatchfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WatchfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
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

        mHourTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourTextPaint.setAntiAlias(true);
        mHourTextPaint.setColor(Color.WHITE);
        mHourTextPaint.setStyle(Style.FILL);
        mHourTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.hour_text_size));
        mHourTextPaint.setTextAlign(Paint.Align.CENTER);

        mComplicationTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mComplicationTextPaint.setColor(Color.WHITE);
        mComplicationTextPaint.setStyle(Style.FILL);
        mComplicationTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mComplicationTextPaint.setTextAlign(Paint.Align.CENTER);

        mComplicationTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mComplicationTitlePaint.setColor(0x88FFFFFF);
        mComplicationTitlePaint.setStyle(Style.FILL);
        mComplicationTitlePaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        mComplicationTitlePaint.setTextAlign(Paint.Align.CENTER);

        mComplicationBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mComplicationBgPaint.setColor(0xEE111111);
        mComplicationBgPaint.setStyle(Style.FILL);

        mComplicationBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mComplicationBoxPaint.setColor(0x33FFFFFF);
        mComplicationBoxPaint.setStyle(Style.STROKE);
        mComplicationBoxPaint.setStrokeWidth(2f);

        setBackgroundColor(mBackgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);

        final int totalMinutes = hour * ONE_HOUR_IN_MIN + minute;
        final float degrees = totalMinutes / MIN_PER_DEGREE - 90f;

        Path path = new Path();

        int width = getWidth();
        int height = getHeight();
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

        // Draw clock face
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

        // Draw dynamically moving complications in the open space
        if (mShowComplications) {
            drawComplications(canvas, viewCenterX, viewCenterY, size, c, degrees);
        }

        // Draw red minute "needle"
        path.reset();
        path.moveTo(-width, viewCenterY);
        path.lineTo(width * 2, viewCenterY);
        canvas.save();
        canvas.rotate(degrees, viewCenterX, viewCenterY);
        canvas.drawPath(path, mLinePaint);
        canvas.restore();
    }

    private void drawComplications(Canvas canvas, int centerX, int centerY, int size, Calendar c, float degrees) {
        float compWidth = size * 0.26f;
        float compHeight = size * 0.13f;
        float cornerRadius = compHeight / 2f;

        float textSp = size * 0.034f;
        float titleSp = size * 0.022f;
        mComplicationTextPaint.setTextSize(textSp);
        mComplicationTitlePaint.setTextSize(titleSp);

        float rad = (float) Math.toRadians(degrees);
        float ux = (float) Math.cos(rad);
        float uy = (float) Math.sin(rad);
        float vx = -uy;
        float vy = ux;

        // Position complications dynamically in the open hemisphere opposite the needle pointer
        float leftSlotCenterX = centerX - ux * 0.22f * size - vx * 0.18f * size;
        float leftSlotCenterY = centerY - uy * 0.22f * size - vy * 0.18f * size;

        float rightSlotCenterX = centerX - ux * 0.22f * size + vx * 0.18f * size;
        float rightSlotCenterY = centerY - uy * 0.22f * size + vy * 0.18f * size;

        // Left Complication Slot (Date)
        RectF leftRect = new RectF(
                leftSlotCenterX - compWidth / 2f,
                leftSlotCenterY - compHeight / 2f,
                leftSlotCenterX + compWidth / 2f,
                leftSlotCenterY + compHeight / 2f
        );

        canvas.drawRoundRect(leftRect, cornerRadius, cornerRadius, mComplicationBgPaint);
        canvas.drawRoundRect(leftRect, cornerRadius, cornerRadius, mComplicationBoxPaint);
        String monthText = mMonthFormat.format(c.getTime()).toUpperCase();
        String dateText = mDateFormat.format(c.getTime()).toUpperCase();

        canvas.drawText(monthText, leftSlotCenterX, leftSlotCenterY - textSp * 0.15f, mComplicationTitlePaint);
        canvas.drawText(dateText, leftSlotCenterX, leftSlotCenterY + textSp * 0.85f, mComplicationTextPaint);

        // Right Complication Slot (Battery / Secondary)
        RectF rightRect = new RectF(
                rightSlotCenterX - compWidth / 2f,
                rightSlotCenterY - compHeight / 2f,
                rightSlotCenterX + compWidth / 2f,
                rightSlotCenterY + compHeight / 2f
        );

        canvas.drawRoundRect(rightRect, cornerRadius, cornerRadius, mComplicationBgPaint);
        canvas.drawRoundRect(rightRect, cornerRadius, cornerRadius, mComplicationBoxPaint);
        canvas.drawText("BATTERY", rightSlotCenterX, rightSlotCenterY - textSp * 0.15f, mComplicationTitlePaint);
        canvas.drawText("85%", rightSlotCenterX, rightSlotCenterY + textSp * 0.85f, mComplicationTextPaint);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        switch (color) {
            case Color.BLACK:
                mHourPaint.setColor(Color.WHITE);
                mHourTextPaint.setColor(Color.WHITE);
                mTenMinutePaint.setColor(Color.WHITE);
                mComplicationTextPaint.setColor(Color.WHITE);
                mComplicationTitlePaint.setColor(0x88FFFFFF);
                mComplicationBgPaint.setColor(0xEE111111);
                mComplicationBoxPaint.setColor(0x33FFFFFF);
                break;
            case Color.WHITE:
                mHourPaint.setColor(Color.BLACK);
                mHourTextPaint.setColor(Color.BLACK);
                mTenMinutePaint.setColor(Color.BLACK);
                mComplicationTextPaint.setColor(Color.BLACK);
                mComplicationTitlePaint.setColor(0x88000000);
                mComplicationBgPaint.setColor(0xEEEEEEEE);
                mComplicationBoxPaint.setColor(0x33000000);
                break;
        }
        invalidate();
    }

    public void setAccentColor(int accentColor) {
        mAccentColor = accentColor;
        mLinePaint.setColor(mAccentColor);
        invalidate();
    }

    public void setShowComplications(boolean show) {
        mShowComplications = show;
        invalidate();
    }

    public boolean isShowComplications() {
        return mShowComplications;
    }
}
