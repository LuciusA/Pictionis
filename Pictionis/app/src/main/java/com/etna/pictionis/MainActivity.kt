package com.etna.pictionis

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            Toast.makeText(this, "Veuiller saisir un email et un mot de passe", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MainActivity", "Email is $email")
        Log.d("MainActivity", "Password is: $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Login succesfully with user id ${it.result.user.uid}")
            }
            .addOnFailureListener {
                Log.d("Main", "Failed to create user: ${it.message}")
                if (it.message == "The email address is badly formatted.") Toast.makeText(this, "Le format de votre email est incorrect", Toast.LENGTH_SHORT).show()
            }
    }
}