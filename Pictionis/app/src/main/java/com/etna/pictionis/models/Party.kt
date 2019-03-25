package com.etna.pictionis.models

class Party(val id: String, val name: String, val createdBy: String, val players: List<User>) {
    constructor() : this("", "", "", emptyList())
}