package com.etna.pictionis.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Party(val id: String, val name: String?, val createdBy: String?, val partyPassword: String?, var players: HashMap<String, String> = HashMap()): Parcelable {
    constructor() : this("", "", "", "")
}