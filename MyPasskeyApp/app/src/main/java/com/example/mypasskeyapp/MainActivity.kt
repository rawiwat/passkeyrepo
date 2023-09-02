package com.example.mypasskeyapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mypasskeyapp.ui.compose.Login
import com.example.mypasskeyapp.ui.compose.MainScreen
import com.example.mypasskeyapp.ui.compose.Note
import com.example.mypasskeyapp.ui.compose.Register
import com.example.mypasskeyapp.ui.theme.MyPasskeyAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private lateinit var myAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        myAuth = Firebase.auth
        val credentialManager = CredentialManager.create(this@MainActivity)
        DataProvider.initSharedPref(applicationContext)
        // Your web app's Firebase configuration
        /*val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey("AIzaSyCUsDHvkq_tVB2WRRYTx9_O4zCB0Yfk-YY")
            .setApplicationId("1:1017839785843:web:e7d970a3c3d2b544b15706")
            .setProjectId("passkey-6197a")
            .setDatabaseUrl("https://passkey-6197a-default-rtdb.firebaseio.com")
            .setStorageBucket("passkey-6197a.appspot.com")
            .setGcmSenderId("1017839785843")
            .build()

        // Initialize Firebase
        FirebaseApp.initializeApp(this, firebaseOptions)*/

        // Initialize Firebase Analytics
        //val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference

        setContent {
            MyPasskeyAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    App(
                        navController as NavHostController,
                        myRef,
                        myAuth,
                        credentialManager,
                        DataProvider.isSignedIn(),
                        this
                    )
                }
            }
        }
    }
}

@Composable
fun App(
    navController: NavHostController,
    myRef: DatabaseReference,
    myAuth: FirebaseAuth,
    credentialManager: CredentialManager,
    signedIn: Boolean,
    context: Context
) {
    NavHost(
        navController = navController,
        startDestination = if (signedIn) "note" else "main"
    )  {
        composable(route = "main") {
            MainScreen(navController)
        }

        composable(route = "login") {
            Login(
                navController = navController,
                myRef = myRef,
                credentialManager = credentialManager,
                context = context,
                myAuth = myAuth
            )
        }

        composable(route = "register") {
            Register(
                navController = navController,
                credentialManager = credentialManager,
                context = context,
                myRef = myRef
            )
        }

        composable(route = "note") {
            Note(
                navController = navController,
                myRef = myRef,
                context = context,
                credentialManager = credentialManager,
                myAuth = myAuth
            )
        }
    }
}

