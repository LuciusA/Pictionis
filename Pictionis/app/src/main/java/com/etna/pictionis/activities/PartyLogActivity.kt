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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
import java.util.ArrayList

class PartyLogActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    val adapter = GroupAdapter<ViewHolder>()

    companion object {
        var currentParty: Party? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_log)

        answer_list_recyclerview_partylog.adapter = adapter

        val party = intent.getParcelableExtra<Party>(PartiesActivity.PARTY_KEY)
        currentParty = party

        supportActionBar?.title = party.name

        listenForMessage()

        prepareSpinner()

        send_answer_button_partylog.setOnClickListener {
            sendAnswerMessage()
        }

        user_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                Log.d("Party", "Spinner selected : ${parent.getItemAtPosition(position)}")
            }

            override fun onNothingSelected(parent: AdapterView<*>){
                Log.d("Party", "Rien du tout")
            }
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

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val LinesTableRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("parties").child(currentParty?.id.toString()).child("lines")
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
                val partyId = party.id
                //val players = party.players

                val ref2 = FirebaseDatabase.getInstance().getReference("/parties/$partyId").child("players")
                ref2.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        //val partyTest = p0.getValue(Party::class.java)
                        val players = p0.value as HashMap<String, String>

                        if (messageChat != null) {
                            if (messageChat.partyId == party.id) {
                                if (messageChat.fromId == FirebaseAuth.getInstance().uid) {
                                    adapter.add(AnswerChatFromItem(messageChat.text))
                                } else {
                                    if (players != null) {
                                        for ((key, value) in players) {
                                            if (key != FirebaseAuth.getInstance().uid) {
                                                adapter.add(AnswerChatToItem(messageChat.text, key, value))
                                            }
                                        }
                                    }
                                }
                            }
                            answer_list_recyclerview_partylog.scrollToPosition(adapter.itemCount - 1)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })
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
                answer_edittext_partylog.text.clear()
                answer_list_recyclerview_partylog.scrollToPosition(adapter.itemCount - 1)
            }
    }

    private fun prepareSpinner() {
        val users: MutableList<String> = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val user = p0.getValue(User::class.java)!!
                users.add(user.username!!)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })

        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, users)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        user_spinner.adapter = aa
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
