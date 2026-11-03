package com.maize.spotlight;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class WatchfaceConfigActivity extends Activity {

    private static final int REQUEST_CODE_RIGHT_COMPLICATION = 1001;
    private static final int REQUEST_CODE_LEFT_COMPLICATION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.BLACK);
        scrollView.setFillViewport(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        int padHorizontal = dpToPx(16);
        int padVertical = dpToPx(36);
        layout.setPadding(padHorizontal, padVertical, padHorizontal, padVertical);

        // App title
        TextView brandView = new TextView(this);
        brandView.setText("SPOTLIGHT");
        brandView.setTextColor(0xAAFF6600);
        brandView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        brandView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        brandView.setGravity(Gravity.CENTER);
        brandView.setLetterSpacing(0.12f);
        layout.addView(brandView);

        TextView titleView = new TextView(this);
        titleView.setText("Complications");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, dpToPx(2), 0, dpToPx(16));
        layout.addView(titleView, titleParams);

        // Right Complication Button
        Button rightSlotBtn = createConfigButton("Right Slot: Day of Week",
                "Default: Mon (pick app or Empty)",
                v -> launchChooser(WatchfaceService.RIGHT_COMPLICATION_ID, REQUEST_CODE_RIGHT_COMPLICATION));
        layout.addView(rightSlotBtn);

        // Left Complication Button
        Button leftSlotBtn = createConfigButton("Left Slot: Month & Date",
                "Default: Nov 02 (pick app or Empty)",
                v -> launchChooser(WatchfaceService.LEFT_COMPLICATION_ID, REQUEST_CODE_LEFT_COMPLICATION));
        LinearLayout.LayoutParams leftParams = (LinearLayout.LayoutParams) leftSlotBtn.getLayoutParams();
        leftParams.setMargins(0, dpToPx(10), 0, dpToPx(16));
        layout.addView(leftSlotBtn);

        // Close / Done button
        TextView doneBtn = new TextView(this);
        doneBtn.setText("Done");
        doneBtn.setTextColor(0x88FFFFFF);
        doneBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        doneBtn.setGravity(Gravity.CENTER);
        doneBtn.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        doneBtn.setOnClickListener(v -> finish());
        layout.addView(doneBtn);

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    private Button createConfigButton(String text, String subtitle, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text + "\n" + subtitle);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setLineSpacing(dpToPx(2), 1f);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(16));
        bg.setColor(0xEE1A1A1A);
        bg.setStroke(dpToPx(1), 0x33FFFFFF);
        button.setBackground(bg);

        int padX = dpToPx(14);
        int padY = dpToPx(10);
        button.setPadding(padX, padY, padX, padY);
        button.setOnClickListener(listener);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);
        return button;
    }

    private void launchChooser(int complicationId, int requestCode) {
        ComponentName componentName = new ComponentName(this, WatchfaceService.class);
        Intent intent = ComplicationHelperActivity.createProviderChooserHelperIntent(
                this,
                componentName,
                complicationId,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SMALL_IMAGE
        );
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            finish();
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
