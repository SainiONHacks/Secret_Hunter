package shop.sainionai.privacyguardian.model

import android.content.pm.ApplicationInfo

/**
 * Coarse app category, derived from ApplicationInfo.category (API 26+).
 * Drives "is this permission expected for this kind of app?" — a flashlight asking
 * for SMS is far more suspicious than a messenger asking for the same.
 */
enum class AppCategory {
    SOCIAL, COMMUNICATION, MAPS, PHOTOGRAPHY, AUDIO, VIDEO, GAME,
    PRODUCTIVITY, NEWS, SHOPPING, FINANCE, TOOL, UNKNOWN;

    companion object {
        fun fromAndroid(category: Int): AppCategory = when (category) {
            ApplicationInfo.CATEGORY_SOCIAL -> SOCIAL
            ApplicationInfo.CATEGORY_MAPS -> MAPS
            ApplicationInfo.CATEGORY_IMAGE -> PHOTOGRAPHY
            ApplicationInfo.CATEGORY_AUDIO -> AUDIO
            ApplicationInfo.CATEGORY_VIDEO -> VIDEO
            ApplicationInfo.CATEGORY_GAME -> GAME
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> PRODUCTIVITY
            ApplicationInfo.CATEGORY_NEWS -> NEWS
            else -> UNKNOWN
        }
    }
}

/**
 * Which permission categories are *reasonable* for a given app category. Anything
 * outside the expected set is treated as elevated risk by the engine; anything inside
 * is discounted. UNKNOWN expects nothing — so unknown apps requesting sensitive perms
 * are scored conservatively (which is the safe default for loan/spyware-style apps
 * that usually report no category).
 */
object CategoryExpectations {
    private val map: Map<AppCategory, Set<PermissionCategory>> = mapOf(
        AppCategory.SOCIAL to setOf(PermissionCategory.CONTACTS, PermissionCategory.CAMERA,
            PermissionCategory.MICROPHONE, PermissionCategory.GALLERY, PermissionCategory.SMS,
            PermissionCategory.NETWORK),
        AppCategory.COMMUNICATION to setOf(PermissionCategory.CONTACTS, PermissionCategory.SMS,
            PermissionCategory.PHONE, PermissionCategory.CAMERA, PermissionCategory.MICROPHONE,
            PermissionCategory.NETWORK),
        AppCategory.MAPS to setOf(PermissionCategory.LOCATION, PermissionCategory.NETWORK),
        AppCategory.PHOTOGRAPHY to setOf(PermissionCategory.CAMERA, PermissionCategory.GALLERY,
            PermissionCategory.STORAGE, PermissionCategory.LOCATION),
        AppCategory.AUDIO to setOf(PermissionCategory.MICROPHONE, PermissionCategory.STORAGE,
            PermissionCategory.NETWORK),
        AppCategory.VIDEO to setOf(PermissionCategory.CAMERA, PermissionCategory.MICROPHONE,
            PermissionCategory.STORAGE, PermissionCategory.NETWORK),
        AppCategory.GAME to setOf(PermissionCategory.NETWORK),
        AppCategory.PRODUCTIVITY to setOf(PermissionCategory.STORAGE, PermissionCategory.CALENDAR,
            PermissionCategory.NETWORK),
        AppCategory.NEWS to setOf(PermissionCategory.NETWORK),
        AppCategory.SHOPPING to setOf(PermissionCategory.LOCATION, PermissionCategory.NETWORK),
        AppCategory.FINANCE to setOf(PermissionCategory.NETWORK),
        AppCategory.TOOL to setOf(PermissionCategory.NETWORK),
        AppCategory.UNKNOWN to emptySet()
    )

    fun isExpected(app: AppCategory, perm: PermissionCategory): Boolean =
        perm == PermissionCategory.NETWORK || (map[app]?.contains(perm) ?: false)
}
