package shop.sainionai.privacyguardian.scanner

import android.Manifest.permission as P
import shop.sainionai.privacyguardian.model.PermissionCategory
import shop.sainionai.privacyguardian.model.PermissionCategory.*

/**
 * Static, versioned mapping of Android permissions -> (category, base risk points).
 *
 * Base points are on a 0..100 conceptual scale and reflect *privacy sensitivity*,
 * not whether the permission is "bad" — context decides that. The RiskEngine adds
 * combo bonuses on top (e.g. SMS + CONTACTS + INTERNET = classic loan-app signature).
 *
 * Keep this table append-only and bump TAXONOMY_VERSION on any change so historical
 * reports remain explainable.
 */
object PermissionClassifier {

    const val TAXONOMY_VERSION = "1.0.0"

    private data class Entry(val category: PermissionCategory, val basePoints: Int)

    private val table: Map<String, Entry> = buildMap {
        // ===== TIER A — high-invasion (drive HIGH/CRITICAL) =====
        // --- Contacts ---
        put(P.READ_CONTACTS, Entry(CONTACTS, 68))
        put(P.WRITE_CONTACTS, Entry(CONTACTS, 50))
        put(P.GET_ACCOUNTS, Entry(CONTACTS, 38))

        // --- SMS (top signal for abusive finance apps) ---
        put(P.READ_SMS, Entry(SMS, 88))
        put(P.RECEIVE_SMS, Entry(SMS, 85))
        put(P.SEND_SMS, Entry(SMS, 78))
        put(P.RECEIVE_MMS, Entry(SMS, 68))

        // --- Call log ---
        put(P.READ_CALL_LOG, Entry(CALL_LOG, 85))
        put(P.WRITE_CALL_LOG, Entry(CALL_LOG, 68))
        put(P.PROCESS_OUTGOING_CALLS, Entry(PHONE, 75))

        // --- Manage-all-files (effectively full storage) ---
        put("android.permission.MANAGE_EXTERNAL_STORAGE", Entry(STORAGE, 92))

        // ===== TIER B — common-sensitive (mostly MEDIUM unless combined) =====
        put(P.READ_PHONE_STATE, Entry(PHONE, 45))
        put(P.READ_PHONE_NUMBERS, Entry(PHONE, 48))

        put(P.ACCESS_FINE_LOCATION, Entry(LOCATION, 55))
        put(P.ACCESS_COARSE_LOCATION, Entry(LOCATION, 38))
        put(P.ACCESS_BACKGROUND_LOCATION, Entry(LOCATION, 70))

        put(P.CAMERA, Entry(CAMERA, 45))
        put(P.RECORD_AUDIO, Entry(MICROPHONE, 52))

        put(P.READ_EXTERNAL_STORAGE, Entry(STORAGE, 40))
        put(P.WRITE_EXTERNAL_STORAGE, Entry(STORAGE, 35))
        put("android.permission.READ_MEDIA_IMAGES", Entry(GALLERY, 45))
        put("android.permission.READ_MEDIA_VIDEO", Entry(GALLERY, 42))
        put("android.permission.READ_MEDIA_AUDIO", Entry(STORAGE, 38))

        put(P.READ_CALENDAR, Entry(CALENDAR, 40))
        put(P.WRITE_CALENDAR, Entry(CALENDAR, 33))
        put(P.BODY_SENSORS, Entry(SENSORS, 45))

        // ===== Network (low alone; matters in combos) =====
        put(P.INTERNET, Entry(NETWORK, 15))
        put(P.ACCESS_NETWORK_STATE, Entry(NETWORK, 5))
    }

    fun categoryOf(permission: String): PermissionCategory =
        table[permission]?.category ?: OTHER

    fun basePointsOf(permission: String): Int =
        table[permission]?.basePoints ?: 0

    /** True for anything we consider privacy-sensitive (i.e. in the table, non-network-noise). */
    fun isSensitive(permission: String): Boolean {
        val e = table[permission] ?: return false
        return e.basePoints >= 30
    }
}
