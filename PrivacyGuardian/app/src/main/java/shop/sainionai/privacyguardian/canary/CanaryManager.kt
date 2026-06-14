package shop.sainionai.privacyguardian.canary

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import shop.sainionai.privacyguardian.model.Canary
import shop.sainionai.privacyguardian.model.CanaryType
import java.io.File
import java.util.UUID

/**
 * Creates decoy resources (Phase 4).
 *
 * - Canary FILE / IMAGE: written into app-private storage; we watch them with a
 *   ContentObserver/FileObserver for access.
 * - Canary CONTACT: inserted into the system contacts provider so a data-harvesting
 *   app that reads all contacts will pick it up. Requires WRITE_CONTACTS.
 *
 * NOTE (Play policy): planting a contact needs WRITE_CONTACTS, which raises review
 * scrutiny. Consider making the contact canary a clearly opt-in research feature, or
 * reserve it for the V2 build, and keep file/image canaries for the Play edition.
 */
class CanaryManager(private val context: Context) {

    fun createFileCanary(): Canary {
        val dir = File(context.filesDir, "canaries").apply { mkdirs() }
        val f = File(dir, "DO_NOT_SHARE.txt")
        f.writeText("Privacy Guardian canary. If you are reading this outside the app, report it.")
        return Canary(UUID.randomUUID().toString(), CanaryType.FILE,
            "DO_NOT_SHARE.txt", f.absolutePath, System.currentTimeMillis())
    }

    fun createImageCanary(): Canary {
        val dir = File(context.filesDir, "canaries").apply { mkdirs() }
        val f = File(dir, "privacy_test.jpg")
        // 1x1 placeholder bytes; a real build would write a valid JPEG.
        f.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()))
        return Canary(UUID.randomUUID().toString(), CanaryType.IMAGE,
            "privacy_test.jpg", f.absolutePath, System.currentTimeMillis())
    }

    /** Inserts a decoy contact named "Privacy Monitor". Caller must hold WRITE_CONTACTS. */
    fun createContactCanary(): Canary {
        val ops = arrayListOf<ContentProviderOperation>()
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build())
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                "Privacy Monitor Canary").build())
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "+10000000000").build())
        val res = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        val uri = res.firstOrNull()?.uri?.toString() ?: ""
        return Canary(UUID.randomUUID().toString(), CanaryType.CONTACT,
            "Privacy Monitor Canary", uri, System.currentTimeMillis())
    }
}
