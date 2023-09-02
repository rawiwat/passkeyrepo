package com.example.mypasskeyapp.ui.compose

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
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

@Composable
fun Note(
    navController: NavController,
    myRef: DatabaseReference,
    credentialManager:CredentialManager,
    context: Context,
    myAuth:FirebaseAuth
) {
    val userName = myAuth.currentUser?.displayName // will get user name later
    val name = myRef.child("Users").child(userName.toString()).toString()
    var note by rememberSaveable {
        mutableStateOf(myRef.child("Users").child(userName.toString()).child("Note").toString())
    }
    
    Surface(Modifier.fillMaxSize()) {
        Column {
            Text(
                text = name
            )
            BasicTextField(
                value = note,
                onValueChange = { note = it }
            ) {
            }

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = {
                    myRef.child("Users").child(userName.toString()).child("Note").push().setValue(note)
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
