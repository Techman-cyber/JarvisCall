package com.jarvis.call

import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

/**
 * This is the piece Android actually calls every time your phone rings.
 * It checks your saved preference (Everyone / Strangers only / Contacts
 * only / Off) and decides whether Jarvis should pick this one up.
 */
class JarvisCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: "unknown"
        Log.d("Jarvis", "Incoming call from: $number")

        val prefs = getSharedPreferences("jarvis_prefs", MODE_PRIVATE)
        val filter = prefs.getString("call_filter", "everyone")
        val ownerName = prefs.getString("owner_name", "the owner") ?: "the owner"

        val response = CallResponse.Builder()
        val isContact = isNumberInContacts(number)

        val shouldJarvisHandle = when (filter) {
            "strangers_only" -> !isContact
            "contacts_only" -> isContact
            "off" -> false
            else -> true // "everyone"
        }

        if (shouldJarvisHandle) {
            // Let the call through so Jarvis can screen/speak.
            // (Actually speaking during the live call needs the
            // InCallService piece — see README "What's not built yet".)
            response
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipNotification(false)
            Log.d("Jarvis", "Jarvis is handling this call for $ownerName")
        } else {
            // Not Jarvis's job this time — let it ring through normally.
            response
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipNotification(false)
        }

        respondToCall(callDetails, response.build())
    }

    private fun isNumberInContacts(number: String): Boolean {
        return try {
            val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI
                .buildUpon()
                .appendPath(number)
                .build()
            val cursor = contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
            val found = cursor != null && cursor.moveToFirst()
            cursor?.close()
            found
        } catch (e: SecurityException) {
            // No contacts permission granted yet — treat as unknown/stranger
            false
        }
    }
}
