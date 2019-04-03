package com.etna.pictionis.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.etna.pictionis.R
import com.etna.pictionis.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        register_button_register.setOnClickListener {
            registerUser()
        }

        already_register_textview_register.setOnClickListener {
            Log.d("MainActivity", "Login activity")

            //launch login activity
            val intent = Intent(this, LoginActivity::class.java)
            Log.d("MainActivity","Intent $intent")
            startActivity(intent)
        }
    }

    private fun registerUser() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter an email and a password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MainActivity", "Email is $email")
        Log.d("MainActivity", "Password is: $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Login succesfully with user id ${it.result.user.uid}")

                saveUserToFirebaseDatabase()
            }
            .addOnFailureListener {
                Log.d("Main", "Failed to create user: ${it.message}")
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                //if (it.message == "The email address is badly formatted.") Toast.makeText(this, "The email address is badly formatted.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirebaseDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString())

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Main", "Finally we saved the user to Firebase Database")

                val intent = Intent(this, PartiesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Main", "Failed to set value to database: ${it.message}")
            }
    }
}