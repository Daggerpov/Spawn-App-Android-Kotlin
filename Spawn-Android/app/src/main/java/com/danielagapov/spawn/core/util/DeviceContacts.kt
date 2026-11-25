package com.danielagapov.spawn.core.util

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone as PhoneKind

/** Phone numbers from the device's contacts, cleaned the way iOS ContactsService does before matching. */
object DeviceContacts {
    /** Needs READ_CONTACTS. Numbers keep a leading "+" when the contact has one; the backend tries the common variants. */
    fun phoneNumbers(context: Context): List<String> {
        val numbers = LinkedHashSet<String>()
        context.contentResolver.query(
            PhoneKind.CONTENT_URI, arrayOf(PhoneKind.NUMBER, PhoneKind.NORMALIZED_NUMBER), null, null, null,
        )?.use { c ->
            while (c.moveToNext()) {
                val raw = c.getString(1)?.takeIf { it.isNotBlank() } ?: c.getString(0) ?: continue
                val cleaned = Phone.clean(raw)
                if (cleaned.count { it.isDigit() } in 7..15) numbers += cleaned
            }
        }
        return numbers.toList()
    }
}
