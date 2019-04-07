package com.etna.pictionis.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MessageChat(val id: String,  val text: String, val partyId: String, val fromId: String, val toId: String?, val timestamp: Long): Parcelable {
    constructor() : this("", "", "", "", "", -1)
}