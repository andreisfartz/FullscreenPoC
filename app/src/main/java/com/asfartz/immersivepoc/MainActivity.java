package com.asfartz.immersivepoc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by Andrei Sfartz on Mar, 2021
 */
public class MainActivity extends AppCompatActivity {

    private Button fullscreenBtn;
    private ConstraintLayout parentCL;
    private ImageView fullscreenStatus;
    private RelativeLayout topRL, bottomRL, leftRL, rightRL;
    private Spinner fullscreenModeSpinner;

    boolean isAPI30 = false;
    boolean isFullscreen = false;
    private WindowInsetsController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentCL = findViewById(R.id.parentCL);
        topRL = findViewById(R.id.topRL);
        bottomRL = findViewById(R.id.bottomRL);
        leftRL = findViewById(R.id.leftRL);
        rightRL = findViewById(R.id.rightRL);
        fullscreenBtn = findViewById(R.id.toggleFullscreenModeBtn);
        fullscreenModeSpinner = findViewById(R.id.fullscreenModeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.fullscreen_modes, android.R.layout.simple_spinner_dropdown_item);
        fullscreenModeSpinner.setAdapter(adapter);
        fullscreenStatus = findViewById(R.id.fullscreenStatusImageView);


        isAPI30 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
        controller = isAPI30 ? parentCL.getWindowInsetsController() : null;

        fullscreenBtn.setOnClickListener(v -> {
            toggleFullscreenMode(isFullscreen);
        });

        if (isAPI30) {
            // Why does this make the app bar transparent, instead of dark (like the app theme is set)
            parentCL.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    if (insets.isVisible(/*WindowInsets.Type.systemBars()*/
                            WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars())) {

                        // Fullscreen, the system bars are not visible.
                        isFullscreen = false;
                        setEnableSpinner(true);
                        fullscreenStatus.setImageResource(R.drawable.ic_red_not_fullscreen);
                        parentCL.setPadding(0, insets.getInsets(WindowInsets.Type.systemBars()).top, 0, insets.getInsets(WindowInsets.Type.systemBars()).bottom);
                    } else {

                        // Not fullscreen, the system bars are visible.
                        isFullscreen = true;
                        setEnableSpinner(false);
                        fullscreenStatus.setImageResource(R.drawable.ic_green_fullscreen);
                    }
                    return insets;
                }
            });
        } else {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {

                        // Not fullscreen, the system bars are visible.
                        isFullscreen = false;
                        setEnableSpinner(true);
                        fullscreenStatus.setImageResource(R.drawable.ic_red_not_fullscreen);
                    } else {

                        // Fullscreen, The system bars are NOT visible.
                        isFullscreen = true;
                        setEnableSpinner(false);
                        fullscreenStatus.setImageResource(R.drawable.ic_green_fullscreen);
                    }
                }
            });
        }


    }

    private void setEnableSpinner(boolean enable) {
        fullscreenModeSpinner.setEnabled(enable);
        fullscreenModeSpinner.setClickable(enable);
    }


    private void toggleFullscreenMode(boolean fullscreenMode) {
        String selectedValue = (String) fullscreenModeSpinner.getSelectedItem();

        if (isAPI30) {
            // API level 30+
            Window window = getWindow();
            if (!fullscreenMode) {
                // false = don't let DecorView handle WindowInsets automatically, we will handle it
                window.setDecorFitsSystemWindows(true);

                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                switch (selectedValue) {
                    // default option. System bars will be forcibly shown on any user interaction on the corresponding display
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
                //true - if apps want the Decor to handle system window fit
                window.setDecorFitsSystemWindows(true);

                // Type.systemBars() --> All system bars. Includes statusBars(), navigationBars() and captionBar(). But not ime().
                controller.show(WindowInsets.Type.systemBars());
            }

        } else {
            // API level < 30
            final View decorView = getWindow().getDecorView();
            if (!fullscreenMode) {

                // WindowInsets cover status bar, navigation bar, keyboard when its open
                // View.SYSTEM_UI_FLAG_LAYOUT_STABLE    --> Use WindowInsets.getInsetsIgnoringVisibility(int) instead to retrieve insets that don't change when system bars change visibility state.
                // View.SYSTEM_UI_FLAG_FULLSCREEN       --> Use WindowInsetsController.hide(int) with WindowInsets.Type.statusBars()
                // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  --> Use WindowInsetsController.hide(int) with WindowInsets.Type.navigationBars()
                // View.SYSTEM_UI_FLAG_IMMERSIVE        --> Use WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
                // View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY --> WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                switch (selectedValue) {
                    case "Lean back":
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                        break;
                    case "Immersive":
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        break;
                    case "Sticky Immersive":
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        break;
                }
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

}