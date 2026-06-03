plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
}
