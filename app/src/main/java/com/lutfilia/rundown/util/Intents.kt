package com.lutfilia.rundown.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.openLink(url: String) {
    val fixed = if (url.startsWith("http", ignoreCase = true)) url else "https://$url"
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fixed)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(this, "Link tidak bisa dibuka", Toast.LENGTH_SHORT).show()
    }
}
