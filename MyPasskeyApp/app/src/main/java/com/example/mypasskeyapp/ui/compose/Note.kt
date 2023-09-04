package com.example.mypasskeyapp.ui.compose

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.navigation.NavController
import com.example.mypasskeyapp.createPasskey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.values

@Composable
fun Note(
    navController: NavController,
    myRef: DatabaseReference,
    credentialManager:CredentialManager,
    context: Context,
    myAuth: FirebaseAuth,
    name: String
) {
    var note by rememberSaveable {
        mutableStateOf(myRef.child("Users").child(name).child("Note").toString())
    }
    
    Surface(Modifier.fillMaxSize()) {
        Column {
            Text(
                text = name
            )
            Card(Modifier.width(250.dp)) {
                BasicTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = {
                    myRef.child("Users").child(name).child("Note").push().setValue(note)
                }
            ) {
                Text("Save Note")
            }
        }

        BackHandler {
            navController.navigate("main")
        }
    }
}
