package com.etna.pictionis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class NewPartyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_party)
        supportActionBar?.title = "Create new party"
    }
}
