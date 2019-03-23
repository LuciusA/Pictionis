package com.etna.pictionis.models

class User(val uid: String, val username: String) {
    constructor() : this("", "")
}