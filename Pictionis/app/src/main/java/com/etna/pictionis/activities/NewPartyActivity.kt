package com.etna.pictionis.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.etna.pictionis.R
import kotlinx.android.synthetic.main.activity_new_party.*
import android.util.Log
import android.widget.Toast
import com.etna.pictionis.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NewPartyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_party)
        supportActionBar?.title = "Create new party"

        create_party_button.setOnClickListener {
            createParty()
        }
    }

    private fun createParty() {
        val name = party_name_edit.text.toString()
        val uid = FirebaseAuth.getInstance().uid ?: ""

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a party name", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("parties")
        val partyId = ref.push().key ?: ""
        val party = Party(partyId, name, uid, emptyList())
        ref.child(partyId).setValue(party)
            .addOnSuccessListener {
                val intent = Intent(this, PartiesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("register party in db failed", "Failed to set value to database: ${it.message}")
            }
    }
}
