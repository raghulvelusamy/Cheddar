plugins {
    id("cheddar.android.feature")
    id("cheddar.android.compose")
}

android {
    namespace = "co.adrianblan.storydetail"
}

dependencies {
    implementation(project(":core:hackernews"))
    implementation(project(":core:webpreview"))
}