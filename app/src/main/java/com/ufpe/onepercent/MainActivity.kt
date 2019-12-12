package com.ufpe.onepercent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*

const val RC_SIGN_IN = 123
//https://www.youtube.com/watch?v=ZC2w2iQQOdo salvou a patria
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //startActivity(Intent(this, MapActivity::class.java))
        //finish()

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        sign_in_button.visibility = View.VISIBLE
        sign_in_button.setSize(SignInButton.SIZE_STANDARD)
        sign_in_button.setOnClickListener{
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
            val acct = GoogleSignIn.getLastSignedInAccount(this)
            if (acct != null) {
                sign_in_button.visibility = View.GONE
                goToMapActivity(acct)
                finish()
            }

            ///Deixando para testes no MI8
            //else{
            //    sign_in_button.visibility = View.GONE
            //    startActivity(Intent(this, MapActivity::class.java))
            //    finish()
           // }
            //TIRAR ISSO DEPOIS
        }




        sign_out_button.setOnClickListener { signOut(mGoogleSignInClient) }
    }

    fun signOut(mGoogleSignInClient: GoogleSignInClient) {
        mGoogleSignInClient.signOut()
        finish()
        startActivity(getIntent())
        Toast.makeText(this, "Signed Out", Toast.LENGTH_LONG).show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            sign_in_button.visibility = View.GONE
            goToMapActivity(account!!)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

            sign_in_button.visibility = View.VISIBLE

        }

    }

    private fun goToMapActivity(account: GoogleSignInAccount) {
        var intent = Intent(this, MapActivity::class.java)
        val username:String = if(account.email == null) "" else account.email!!.split("@")[0]
        intent.putExtra("photoUrl", account.photoUrl.toString())
        intent.putExtra("username", username)
        intent.putExtra("id", account.id)
        println("************************** "+ account.id)
        startActivity(intent)
    }
}

