package com.lilbro.picobotella

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry-point required by Hilt.
 *
 * The [@HiltAndroidApp] annotation triggers Hilt's code generation and
 * installs the application-level component that serves as the root of
 * the dependency hierarchy.
 *
 * Remember to declare this class in AndroidManifest.xml:
 * ```xml
 * <application
 *     android:name=".HiltApplication"
 *     ... />
 * ```
 */
@HiltAndroidApp
class HiltApplication : Application()
