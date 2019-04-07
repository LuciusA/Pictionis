package com.etna.pictionis.activities

import android.content.Context
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import com.etna.pictionis.R
import com.etna.pictionis.models.MessageChat
import com.etna.pictionis.models.Party
import com.etna.pictionis.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_party_log.*
import kotlinx.android.synthetic.main.answer_chat_row.view.*
import kotlinx.android.synthetic.main.answer_chat_row2.view.*

class PartyLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    private val LinesTableRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("lines")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_log)

        answer_list_recyclerview_partylog.adapter = adapter

        val party = intent.getParcelableExtra<Party>(PartiesActivity.PARTY_KEY)

        supportActionBar?.title = party.name

        listenForMessage()

        send_answer_button_partylog.setOnClickListener {
            sendAnswerMessage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pictionis, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
        when (item?.itemId) {
            R.id.action_clear -> consumeMenuSelected { removeFirebaseChild() }
            else              -> super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()
        LinesTableRef.addChildEventListener(DrawingLinesRefListener)
        drawView.drawListener = { lineList ->
            lineList?.let { sendToFirebase(lineList) }
        }
    }

    override fun onStop() {
        super.onStop()
        LinesTableRef.removeEventListener(DrawingLinesRefListener)
        drawView.drawListener = null
    }

    private fun sendToFirebase(lineList: List<Point>) {
        LinesTableRef.push().setValue(lineList)
    }

    private fun removeFirebaseChild() {
        LinesTableRef.removeValue()
    }

    private fun clearDrawView() {
        drawView.clear()
    }

    private fun drawLine(lineList: List<Point>) {
        drawView.drawLine(lineList)
    }

    private val DrawingLinesRefListener = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            Log.e("Party", "onChildAdded")
            p0?.children
                ?.map { children -> children.getValue<Point>(Point::class.java) }
                ?.let { lineList -> drawLine(lineList as List<Point>) }
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            Log.e("Party", "onChildRemoved")
            clearDrawView()
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    inline fun consumeMenuSelected(func: () -> Unit): Boolean {
        func()
        return true
    }


    private fun listenForMessage() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val messageChat = p0.getValue(MessageChat::class.java)
                val party = intent.getParcelableExtra<Party>(PartiesActivity.PARTY_KEY)
                val players = party.players

                if (messageChat != null) {
                    Log.d("Party", "Ceci esdt le ${messageChat.text}")

                    if (messageChat.partyId == party.id) {
                        if (messageChat.fromId == FirebaseAuth.getInstance().uid) {
                            adapter.add(AnswerChatFromItem(messageChat.text))
                        } else {
                            for ((key, value) in players) {
                                if (key != FirebaseAuth.getInstance().uid) {
                                    adapter.add(AnswerChatToItem(messageChat.text, key, value))
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun sendAnswerMessage() {
        val text = answer_edittext_partylog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val party = intent.getParcelableExtra<Party>(PartiesActivity.PARTY_KEY)
        val partyId = party.id

        if (fromId == null) return

        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val messageChat = MessageChat(ref.key!!, text, partyId, fromId, "", System.currentTimeMillis() / 1000)
        ref.setValue(messageChat)
            .addOnSuccessListener {
                Log.d("Party", "Saved our message: ${ref.key}")
            }
    }
}

class AnswerChatFromItem(val text: String): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_answer_message_to.text = text
    }

    override fun getLayout(): Int {
        return R.layout.answer_chat_row2
    }
}

class AnswerChatToItem(val text: String, val userId: String, val username: String): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_answer_message_from.text = text
        viewHolder.itemView.textview_username_message_from.text = username
    }

    override fun getLayout(): Int {
        return R.layout.answer_chat_row
    }
}
