package com.etna.pictionis.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String?, val username: String?): Parcelable {
    constructor() : this("", "")

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "username" to username
        )
    }
}