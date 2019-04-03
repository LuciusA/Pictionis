package com.etna.pictionis.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.util.Log
import com.etna.pictionis.R
import com.etna.pictionis.models.Party
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_parties.*
import kotlinx.android.synthetic.main.party_row_parties.view.*

class PartiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parties)
        supportActionBar?.title = "Parties"

        verifyUserIsLoggedIn()

        val adapter = GroupAdapter<ViewHolder>()

        recyclerview_parties.adapter = adapter

        fetchParties()
    }

    private fun fetchParties() {
        val ref = FirebaseDatabase.getInstance().getReference("/parties")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach{
                    Log.d("Parties", it.toString())
                    val party = it.getValue(Party::class.java)
                    if (party != null) {
                        adapter.add(PartyItem(party))
                    }
                }

                recyclerview_parties.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    // redirect user to the login page if not logged in
    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_new_party -> {
                val intent = Intent(this, NewPartyActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

class PartyItem(val party: Party): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.partyname_textview_parties.text = party.name
    }

    override fun getLayout(): Int {
        return R.layout.party_row_parties
    }
}
