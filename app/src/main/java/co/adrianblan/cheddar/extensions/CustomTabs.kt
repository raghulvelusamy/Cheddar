package co.adrianblan.cheddar.extensions

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import javax.inject.Inject
import javax.inject.Singleton

interface CustomTabsLauncher {
    fun launchUrl(url: String)
}

@Singleton
class CustomTabsLauncherImpl
@Inject constructor(
    private val context: Context
) : CustomTabsLauncher {
    override fun launchUrl(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .apply {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            .launchUrl(context, url.toUri())
    }
}