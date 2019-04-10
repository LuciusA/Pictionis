package com.etna.pictionis.activities

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.util.Log
import com.etna.pictionis.R
import com.etna.pictionis.models.Party
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_parties.*
import kotlinx.android.synthetic.main.party_row_parties.view.*
import android.content.DialogInterface
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.etna.pictionis.models.MessageChat
import com.etna.pictionis.models.User
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_party_log.*
import kotlinx.android.synthetic.main.dialog_password.*


class PartiesActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parties)
        supportActionBar?.title = "Parties"

        fetchCurrentUser()

        verifyUserIsLoggedIn()

        recyclerview_parties.adapter = adapter

        fetchParties()

        //listenForParty()
    }

    companion object {
        val PARTY_KEY = "PARTY_KEY"
        val USER_KEY = "USER_KEY"
        var currentUser: User? = null
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("Party", "Current user is ${currentUser?.username}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun fetchParties() {
        val ref = FirebaseDatabase.getInstance().getReference("/parties")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val party = p0.getValue(Party::class.java)
                if (party != null) {
                    adapter.add(PartyItem(party))
                }

                adapter.setOnItemClickListener { item, view ->
                    val partyItem = item as PartyItem
                    val partyId = partyItem.party.id
                    val partyPassword = partyItem.party.partyPassword

                    val builder = AlertDialog.Builder(this@PartiesActivity)

                    val inflater = layoutInflater

                    val myLayout = inflater.inflate(R.layout.dialog_password, null)

                    builder.setView(myLayout)
                        //set positive button
                        .setPositiveButton("Validate",
                            DialogInterface.OnClickListener { dialog, id ->
                                val passwordEditText = myLayout.findViewById<EditText>(R.id.password_edittext_access_party)
                                val password = passwordEditText.text.toString()

                                if (password.isEmpty()) {
                                    Toast.makeText(this@PartiesActivity, "Please enter the password", Toast.LENGTH_SHORT).show()
                                    return@OnClickListener
                                }

                                if (password == partyPassword) {
                                    val partyItem = item as PartyItem

                                    val ref = FirebaseDatabase.getInstance().getReference("parties")
                                    val userId = currentUser?.uid ?: ""

                                    ref.child(partyId).child("players").child(userId).setValue(currentUser?.username)
                                        .addOnSuccessListener {
                                            val intent = Intent(view.context, PartyLogActivity::class.java)
                                            intent.putExtra(PARTY_KEY, partyItem.party)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener {
                                            Log.d("PartyActivity", "Failed to set value to database: ${it.message}")
                                        }
                                } else {
                                    Toast.makeText(this@PartiesActivity, "Wrong password", Toast.LENGTH_SHORT).show()
                                }

                            })
                        .setNegativeButton("Cancel",
                            DialogInterface.OnClickListener { dialog, id ->
                                dialog.cancel()
                            })
                        .show()
                }

                recyclerview_parties.adapter = adapter
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

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
        viewHolder.itemView.partyname_textview_parties.setTag(R.string.id, party.id)
        viewHolder.itemView.partyname_textview_parties.setTag(R.string.password, party.partyPassword)
    }

    override fun getLayout(): Int {
        return R.layout.party_row_parties
    }
}
