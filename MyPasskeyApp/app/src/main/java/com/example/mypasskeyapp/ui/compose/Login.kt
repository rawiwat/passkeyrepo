package com.example.mypasskeyapp.ui.compose

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavHostController
import com.example.mypasskeyapp.DataProvider
import com.example.mypasskeyapp.fetchAuthJsonFromServer
import com.example.mypasskeyapp.handleFailureGet
import com.example.mypasskeyapp.handleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Login(
    navController: NavHostController,
    myRef: DatabaseReference,
    myAuth: FirebaseAuth,
    credentialManager: CredentialManager,
    context: Context
) {
    var userName by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }
    val getPasswordOption = GetPasswordOption()

    val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
        requestJson = fetchAuthJsonFromServer(context)
    )

    val getCredRequest = GetCredentialRequest(
        listOf(getPasswordOption, getPublicKeyCredentialOption)
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(Modifier.width(250.dp)) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Text(text = "Name:")
                    BasicTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(Modifier.width(250.dp)) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Text(text = "Password:")
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }


            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                }
            }) {
                Text(text = "Login With Password")
            }

            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        /*val result = credentialManager.getCredential(
                            // Use an activity-based context to avoid undefined system UI
                            // launching behavior.
                            context = context,
                            request = getCredRequest
                        )
                        handleSignIn(result, myAuth, myRef,context,navController,userName).let {
                            sendSignInResponseToServer()
                            navController.navigate("note/$userName")
                        }*/
                        //
                        val data = getSavedCredentials(credentialManager, context)
                        data?.let {
                            sendSignInResponseToServer()
                            navController.navigate("note/$userName")
                        }
                    } catch (e : GetCredentialException) {
                        println("get credential error: ,$e")
                        handleFailureGet(e, context = context)
                    }
                }
            }) {
                Text("Login With Passkey")
            }
        }
    }
}

fun sendSignInResponseToServer(): Boolean {
    return true
}


suspend fun getSavedCredentials(credentialManager: CredentialManager,context:Context): String? {
    val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(fetchAuthJsonFromServer(context), null)
    val getPasswordOption = GetPasswordOption()
    val result = try {
        credentialManager.getCredential(
            context,
            GetCredentialRequest(
                listOf(
                    getPublicKeyCredentialOption,
                    getPasswordOption
                )
            )
        )
  } catch (e: Exception) {
        Log.e("Error","An error occurred while authenticating through saved credentials")
        Log.e("Auth", "getCredential failed with exception: " + e.message.toString())
        Toast.makeText(context,e.message,Toast.LENGTH_LONG).show()
        return null
    }
    val credential = result.credential
    if (credential is PublicKeyCredential) {
        return credential.authenticationResponseJson
    }
    if (credential is PasswordCredential) {
        DataProvider.setSignedInThroughPasskeys(false)
        return "${credential.id},${credential.password}"
    }
    /*if (result.credential is CustomCredential) {
        //If you are also using any external sign-in libraries, parse them here with the
        // utility functions provided.
    }*/
    return null
}