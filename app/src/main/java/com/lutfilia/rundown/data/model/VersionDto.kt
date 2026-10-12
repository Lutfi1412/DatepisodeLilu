package com.lutfilia.rundown.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response GET get-version:
 * { "status": true, "message": "...", "data": { "link": "https://...", "version": "2.0" } }
 */
data class VersionResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: VersionData? = null
)

data class VersionData(
    val link: String? = null,
    // "vesion" (typo di contoh lama) tetap dibaca supaya aman
    @SerializedName(value = "version", alternate = ["vesion"])
    val version: String? = null
)

/** Model yang dipakai aplikasi. */
data class LatestVersion(
    val version: String,
    val link: String
)