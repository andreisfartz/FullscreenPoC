package com.asfartz.immersivepoc;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Created by Andrei Sfartz on Mar, 2021
 */
public class MainActivityTest {

    ActivityTestRule<MainActivity> mainActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void setUp() throws Exception {
    }


}