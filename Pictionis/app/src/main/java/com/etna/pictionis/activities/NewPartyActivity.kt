package com.etna.pictionis.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.etna.pictionis.R

class NewPartyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_party)
        supportActionBar?.title = "Create new party"
    }
}