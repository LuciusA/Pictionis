package com.etna.pictionis.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.etna.pictionis.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        register_button_login.setOnClickListener{
            loginUser()
        }

        create_account_textview_login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            Log.d("MainActivity","Intent $intent")
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter an email and a password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("Main", "Email is $email")
        Log.d("Main", "Password is: $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Login succesfully with user id ${it.result}")

                val intent = Intent(this, PartiesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Main", "Failed to login user: ${it.message}")
                if (it.message == "The email address is badly formatted.") Toast.makeText(this, "The email address is badly formatted.", Toast.LENGTH_SHORT).show()
            }
    }
}