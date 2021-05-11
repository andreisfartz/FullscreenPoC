package com.asfartz.immersivepoc;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;

import static org.junit.Assert.*;

public class MainActivityTest {

    @Rule
    ActivityTestRule<MainActivity> mainActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    @Before
    public void setUp() throws Exception {
    }

    public void testViewsWhenNotFullscreen() {

    }


}