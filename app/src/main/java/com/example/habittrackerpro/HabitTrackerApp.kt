package com.example.habittrackerpro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class required for Hilt.
 * The @HiltAndroidApp annotation triggers Hilt's code generation, including a base
 * class for your application that serves as the application-level dependency container.
 */

@HiltAndroidApp
class HabitTrackerApp : Application()
