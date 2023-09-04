package com.example.mypasskeyapp.ui.compose

import android.content.Context
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
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePasswordResponse
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.navigation.NavController
import com.example.mypasskeyapp.CreatePasskeyResponseData
import com.example.mypasskeyapp.DataProvider
import com.example.mypasskeyapp.createPasskey
import com.example.mypasskeyapp.ui.theme.MyPasskeyAppTheme
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Register(
    navController: NavController,
    credentialManager: CredentialManager,
    context: Context,
    myRef:DatabaseReference
) {
    var name by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var registerWithPassword by rememberSaveable {
        mutableStateOf(false)
    }

    MyPasskeyAppTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(Modifier.width(250.dp)) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Text(text = "Name:")
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if(registerWithPassword) {
                    Card(Modifier.width(250.dp)) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            Text(text = "Password:")
                            BasicTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    onClick = {
                        if (registerWithPassword && password.isNotEmpty() && name.isNotEmpty()) {
                            registerPassword(
                                name,
                                password,
                                context,
                                credentialManager,
                                navController)
                        } else {
                            registerWithPassword = true
                        }
                    }
                ) {
                    Text("Register with Password")
                }

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (name.isNotEmpty()) {
                                val data = createPasskey(
                                    credentialManager,
                                    context,
                                    name
                                )

                                data.let {
                                    val responseData = Gson().fromJson(
                                        (data as CreatePublicKeyCredentialResponse).registrationResponseJson,CreatePasskeyResponseData::class.java
                                    )
                                    println(responseData)
                                    myRef.child("Users").child(name).child("passkey").push().setValue(responseData)
                                    DataProvider.setSignedInThroughPasskeys(true)
                                    navController.navigate("note/$name")
                                }
                            } else {
                                Toast.makeText(context, "Name can't be empty", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                ) {
                    Text("Register with Passkey")
                }
            }
        }
    }
}

fun registerPassword(
    username: String,
    password: String,
    context: Context,
    credentialManager:CredentialManager,
    navController: NavController
) {
    // Initialize a CreatePasswordRequest object.
    val createPasswordRequest = CreatePasswordRequest(
        id = username,
        password = password
    )

    // Create credential and handle result.
    CoroutineScope(Dispatchers.Main).launch {
        try {
            credentialManager.createCredential(
                    // Use an activity based context to avoid undefined
                    // system UI launching behavior.
            context, createPasswordRequest
            ) as CreatePasswordResponse
            navController.navigate("note{$username}")
        } catch (e: CreateCredentialException) {
            Toast.makeText(context,e.message,Toast.LENGTH_SHORT).show()
        }
    }
}