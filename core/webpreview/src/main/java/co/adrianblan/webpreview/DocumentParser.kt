package co.adrianblan.webpreview

import co.adrianblan.common.baseUrl
import co.adrianblan.common.completePartialUrl
import co.adrianblan.common.urlSiteName
import co.adrianblan.model.WebPreviewData
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


private const val OG_SITE_NAME = "og:site_name"
private const val OG_DESCRIPTION = "og:description"
private const val OG_IMAGE = "og:image"

private const val APPLE_ICON_PRECOMPOSED = "apple-touch-icon-precomposed"
private const val APPLE_ICON = "apple-touch-icon"
private const val SHORTCUT_ICON = "shortcut icon"
private const val ICON = "icon"
internal fun Document.toWebPreviewData(url: String): WebPreviewData {

    val baseUrl = url.baseUrl()

    // Get Open Graph tags
    val ogTags = head()
        .select("meta[property^=og:]")

    val siteName = ogTags.getOgContentOrNull(OG_SITE_NAME)
        ?.takeIf { it.isNotEmpty() }
        ?: baseUrl.urlSiteName()

    val description = ogTags.getOgContentOrNull(OG_DESCRIPTION)
        ?.replace("\n\n", " ")
        ?.takeIf { it.isNotEmpty() }

    val imageUrl = ogTags.getOgContentOrNull(OG_IMAGE)
        ?.takeImageUrlIfCompatible()
        ?.completePartialUrl(baseUrl)
        ?.takeIf { it.isNotEmpty() }

    // Make best effort to get icon tags
    val iconTags = head()
        .select("link[rel]")

    val iconUrl =
        (iconTags.getIconContentOrNull(APPLE_ICON_PRECOMPOSED)?.takeImageUrlIfCompatible()
            ?: iconTags.getIconContentOrNull(APPLE_ICON)?.takeImageUrlIfCompatible()
            ?: iconTags.getIconContentOrNull(ICON)?.takeImageUrlIfCompatible()
            ?: iconTags.getIconContentOrNull(SHORTCUT_ICON)?.takeImageUrlIfCompatible())
            ?.completePartialUrl(baseUrl)
            ?.takeIf { it.isNotEmpty() }

    return WebPreviewData(
        siteName = siteName,
        description = description,
        imageUrl = imageUrl,
        iconUrl = iconUrl,
        favIconUrl = "$baseUrl/favicon.ico"
    )
}

private fun List<Element>.getOgContentOrNull(propertyName: String): String? =
    firstOrNull { it.attr("property") == propertyName }
        ?.attr("content")

private fun List<Element>.getIconContentOrNull(propertyName: String): String? =
    filter { it.attr("rel") == propertyName }
        .maxByOrNull { it.attr("sizes") }
        ?.attr("href")

private fun String.takeImageUrlIfCompatible(): String? {
    val ending = this.split("?")[0]

    // We can't render svg as is
    val forbiddenTypes = listOf(".svg")

    return if (forbiddenTypes.any { ending.endsWith(it) }) null
    else this
}