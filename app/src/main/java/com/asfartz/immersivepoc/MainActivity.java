package com.asfartz.immersivepoc;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Andrei Sfartz on Mar, 2021
 */
@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button fullscreenBtn;
    private ConstraintLayout parentView;
    private ImageView fullscreenStatus;
    private RelativeLayout topRL, bottomRL, leftRL, rightRL;
    private ScrollView mScrollView;
    private Spinner fullscreenModeSpinner;
    private TextView insetsTV;
    private View decorView;

    private boolean isMinAPI30 = false;
    private boolean isFullscreen = false;
    private WindowInsetsController controller;

    private void initViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // if we don't do this, because we set setDecorFitsSystemWindows(false), the system will place the content/decor as it is, and the ActionBar will cover the content
            // if it was setDecorFitsSystemWindows(true), the content/decor would've been placed to fit with the system windows ( = take their insets into account)
            actionBar.hide();
        }

        parentView = findViewById(R.id.parentLayout);
        topRL = findViewById(R.id.topRect);
        bottomRL = findViewById(R.id.bottomRect);
        leftRL = findViewById(R.id.leftRect);
        rightRL = findViewById(R.id.rightRect);

        fullscreenBtn = findViewById(R.id.toggleFullscreenModeBtn);
        fullscreenModeSpinner = findViewById(R.id.fullscreenModeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.fullscreen_modes, android.R.layout.simple_spinner_dropdown_item);
        fullscreenModeSpinner.setAdapter(adapter);
        fullscreenStatus = findViewById(R.id.fullscreenStatusImageView);

        mScrollView = findViewById(R.id.mScrollView);
        decorView = getWindow().getDecorView();
        insetsTV = findViewById(R.id.insetsLogsTV);

        isMinAPI30 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
        controller = isMinAPI30 ? parentView.getWindowInsetsController() : null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        fullscreenBtn.setOnClickListener(v -> {
            toggleFullscreenMode(isFullscreen);
        });

        if (isMinAPI30) {
            // With stick immersive, you cannot receive a callback when the system UI visibility changes.
            // So if you want the auto-hiding behavior of sticky immersive mode, but still want to know when the system UI re-appears
            // in order to show your own UI controls as well, use the regular IMMERSIVE flag and use Handler.postDelayed()
            // or something similar to re-enter immersive mode after a few seconds.

            // Why does using decorView.setOnApplyWindowInsetsListener(...) make the system bars transparent, instead of what the app theme tells it to be?
            // It's initially colored according to the theme, then it becomes transparent. WTF ???
            // Conclusion: DON't apply WindowInsetsListener to DecorView, I guess...
            parentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    logInsets(insets);

                    // While in Lean Back mode, clicking anywhere will reveal ONLY the navigation bar, we have to manually drag the status bar to make it appear
                    if (insets.isVisible(WindowInsets.Type.navigationBars()) || insets.isVisible(WindowInsets.Type.statusBars())) {
                        // Not fullscreen, the system bars are visible.
                        enableSpinner(true);
                        isFullscreen = false;
                        fullscreenStatus.setImageResource(R.drawable.ic_red_not_fullscreen);
                        parentView.setPadding(
                                0, insets.getInsets(WindowInsets.Type.systemBars()).top,
                                0, insets.getInsets(WindowInsets.Type.systemBars()).bottom);
                    } else {
                        // Fullscreen, the system bars are not visible.
                        enableSpinner(false);
                        isFullscreen = true;
                        fullscreenStatus.setImageResource(R.drawable.ic_green_fullscreen);
                        parentView.setPadding(
                                0, insets.getInsets(WindowInsets.Type.systemBars()).top,
                                0, insets.getInsets(WindowInsets.Type.systemBars()).bottom);
                    }
                    return insets;
                }
            });

        } else {
            /*parentView*/decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        // The system bars are visible. Activity is not fullscreen
                        isFullscreen = false;
                        enableSpinner(true);
                        fullscreenStatus.setImageResource(R.drawable.ic_red_not_fullscreen);
                    } else {
                        // The system bars are NOT visible. Activity is fullscreen
                        isFullscreen = true;
                        enableSpinner(false);
                        fullscreenStatus.setImageResource(R.drawable.ic_green_fullscreen);
                    }
                }
            });
        }

    }

    private void toggleFullscreenMode(boolean isCurrentlyFullscreen) {
        String selectedValue = (String) fullscreenModeSpinner.getSelectedItem();

        if (isMinAPI30) {
            // >= API30
            Window window = getWindow();
            if (!isCurrentlyFullscreen) {
                // false = don't let DecorView handle WindowInsets automatically, we will handle it
                window.setDecorFitsSystemWindows(false);

                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                switch (selectedValue) {
                    // Default option. System bars will be forcibly shown on any user interaction on the corresponding display
                    case "Lean back":
                        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH);
                        break;

                    // System bars will be revealed with system gestures, such as swiping from the edge of the screen where the bar is hidden from.
                    case "Immersive":
                        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE);
                        break;

                    // System bars can be revealed temporarily with system gestures, such as swiping from the edge of the screen where the bar is hidden from.
                    // Will overlay app's content, may have some degree of transparency, and will automatically hide after a short timeout.
                    case "Sticky Immersive":
                        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        break;
                }
            } else {
                // true - if apps want the Decor to fit 'properly with the system windows ( = to be inseted by it)
                // false - place the decor 'as is', and allow it to handle the system windows insets

                window.setDecorFitsSystemWindows(false);
                // NOTE: if above line is set to true, "sticky immersive" doesn't send insets changes to the listener. needs to be false in this case. Why???

                // Answer: https://developer.android.com/training/system-ui/immersive
                // With stick immersive, you cannot receive a callback when the system UI visibility changes.
                // So if you want the auto-hiding behavior of sticky immersive mode, but still want to know when the system UI re-appears
                // in order to show your own UI controls as well, use the regular IMMERSIVE flag and use Handler.postDelayed()
                // or something similar to re-enter immersive mode after a few seconds.

                // Despite the above answer, it doesn't seem to be the true on API 30

                // Type.systemBars() --> All system bars. Includes statusBars(), navigationBars() and captionBar(). But not ime().
                controller.show(WindowInsets.Type.systemBars());
            }

        } else {
            // API level < 30
            final View decorView = getWindow().getDecorView();
            if (!isCurrentlyFullscreen) {
                // WindowInsets cover status bar, navigation bar, keyboard when its open
                // View.SYSTEM_UI_FLAG_LAYOUT_STABLE    --> Use WindowInsets.getInsetsIgnoringVisibility(int) instead to retrieve insets that don't change when system bars change visibility state.
                // View.SYSTEM_UI_FLAG_FULLSCREEN       --> Use WindowInsetsController.hide(int) with WindowInsets.Type.statusBars()
                // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  --> Use WindowInsetsController.hide(int) with WindowInsets.Type.navigationBars()
                // View.SYSTEM_UI_FLAG_IMMERSIVE        --> Use WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
                // View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY --> WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                switch (selectedValue) {
                    case "Lean back":
                        // Default option. System bars will be forcibly shown on any user interaction on the corresponding display
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                        break;
                    case "Immersive":
                        // System bars will be revealed with system gestures, such as swiping from the edge of the screen where the bar is hidden from.
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        break;
                    // System bars can be revealed temporarily with system gestures, such as swiping from the edge of the screen where the bar is hidden from.
                    // Will overlay app's content, may have some degree of transparency, and will automatically hide after a short timeout.
                    case "Sticky Immersive":
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        break;
                }
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    private void enableSpinner(boolean enable) {
        fullscreenModeSpinner.setEnabled(enable);
        fullscreenModeSpinner.setClickable(enable);
    }

    private void logInsets(WindowInsets windowInsets) {
        StringBuilder sb = new StringBuilder(insetsTV.getText());
        sb.append("\nsystemBars: ").append(windowInsets.getInsets(WindowInsets.Type.systemBars()))
                .append("\nnavigationBars: ").append(windowInsets.getInsets(WindowInsets.Type.navigationBars()))
                .append("\nstatusBars: ").append(windowInsets.getInsets(WindowInsets.Type.statusBars()))
                .append("\nnavigationBars: ").append(windowInsets.getInsets(WindowInsets.Type.navigationBars()))
                .append("\ncaptionBar: ").append(windowInsets.getInsets(WindowInsets.Type.captionBar()))
                .append("\nime: ").append(windowInsets.getInsets(WindowInsets.Type.ime()))
                .append("\ndisplayCutout: ").append(windowInsets.getInsets(WindowInsets.Type.displayCutout()))
                .append("\nmandatorySystemGestures: ").append(windowInsets.getInsets(WindowInsets.Type.mandatorySystemGestures()))
                .append("\nsystemGestures: ").append(windowInsets.getInsets(WindowInsets.Type.systemGestures()))
                .append("\ntappableElement: ").append(windowInsets.getInsets(WindowInsets.Type.tappableElement()))
                .append("\n------------------------------------------------------------------------------\n");
        insetsTV.setText(sb.toString());
        mScrollView.post(() -> mScrollView.fullScroll(View.FOCUS_DOWN));
        Log.d(TAG, sb.toString());
    }
}