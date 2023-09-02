package com.example.mypasskeyapp

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.gson.annotations.SerializedName
import java.security.SecureRandom
import java.security.Signature




fun handleFailureGet(e: GetCredentialException) {
    Log.w(ContentValues.TAG, "exception type ${e::class.java.name}")
}
fun handleFailureCreate(e: CreateCredentialException) {
    when (e) {
        is CreatePublicKeyCredentialDomException -> {
            // Handle the passkey DOM errors thrown according to the
            // WebAuthn spec.
            //handlePasskeyError(e.domError) TBA
            Log.w(ContentValues.TAG, "Unexpected exception type ${e::class.java.name}")
        }
        is CreateCredentialCancellationException -> {
            // The user intentionally canceled the operation
            // and chose not to register the credential.
            Log.w(ContentValues.TAG, "Unexpected exception type ${e::class.java.name}")
        }
        is CreateCredentialInterruptedException -> {
            // Retry-able error. Consider retrying the call.
            Log.w(ContentValues.TAG, "Unexpected exception type ${e::class.java.name}")
        }

        is CreateCredentialProviderConfigurationException -> {
            // Your app is missing the provider configuration dependency.
            // Most likely, you're missing the
            // "credentials-play-services-auth" module.
            Log.w(ContentValues.TAG, "Unexpected exception type ${e::class.java.name}")
        }

        is CreateCredentialUnknownException -> {
            Log.w(ContentValues.TAG, "Unexpected exception type ${e::class.java.name}")
        }

        is CreateCredentialCustomException -> {
            // You have encountered an error from a 3rd-party SDK. If you
            // make the API call with a request object that's a subclass of
            // CreateCustomCredentialRequest using a 3rd-party SDK, then you
            // should check for any custom exception type constants within
            // that SDK to match with e.type. Otherwise, drop or log the
            // exception.
            Log.w(ContentValues.TAG, "Third Party SDK exception type ${e::class.java.name}")
        }
        else -> Log.w(ContentValues.TAG, "Unexpected exception type ${e::class.java.name}")
    }
}

fun handleSignIn(
    result: GetCredentialResponse,
    myAuth: FirebaseAuth,
    myRef: DatabaseReference,
    context: Context,
    navController: NavHostController
) {
    // Handle the successfully returned credential.
    when (val credential = result.credential) {
        is PublicKeyCredential -> {
            val responseJson = credential.authenticationResponseJson
            // Share responseJson i.e. a GetCredentialResponse on your server to
            // validate and authenticate
        }

        is PasswordCredential -> {
            val username = credential.id
            val password = credential.password
            // Use id and password to send to your server to validate
            // and authenticate
            myAuth.signInWithEmailAndPassword(username,password)
                .addOnCompleteListener(MainActivity()){ task->
                    if (task.isSuccessful) {
                        Toast.makeText(context,"Login Successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = task.exception?.message
                        Toast.makeText(context,"Login Failed,$errorMessage", Toast.LENGTH_SHORT).show()
                        Log.e("e","Login Failed,$errorMessage")
                        println("Login Failed,$errorMessage")
                    }
                }
        } else -> {
        // Catch any unrecognized credential type here.
        Log.e(ContentValues.TAG, "Unexpected type of credential")
    }
    }
}

suspend fun createPasskey(
    credentialManager: CredentialManager,
    context: Context,
    name: String
): CreatePublicKeyCredentialResponse? {
    // Contains the request in JSON format. Uses the standard WebAuthn
    // web JSON spec.
    // Execute CreateCredentialRequest asynchronously to register credentials
    // for a user account. Handle success and failure cases with the result and
    // exceptions, respectively.
    val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
        requestJson = fetchRegistrationJsonFromServer(context, name),
        preferImmediatelyAvailableCredentials = false
    )
    var result: CreatePublicKeyCredentialResponse? = null
    try {
        result = credentialManager.createCredential(
            // Use an activity-based context to avoid undefined system
            // UI launching behavior
            context = context,
            request = createPublicKeyCredentialRequest,
        ) as CreatePublicKeyCredentialResponse
    } catch (e: CreateCredentialException) {
        Log.e("E", e.errorMessage.toString())
        Toast.makeText(context,e.errorMessage,Toast.LENGTH_LONG).show()
    }
    return result
}

data class CreatePasskeyResponseData(
    @SerializedName("response") val response: Response,
    @SerializedName("id") val id: String,
    @SerializedName("rawId") val rawId: String,
    @SerializedName("type") val type: String
) {
    data class Response(
        @SerializedName("clientDataJSON") val clientDataJSON: String,
        @SerializedName("authenticatorData") val authenticatorData: String,
        @SerializedName("signature") val signature: String,
        @SerializedName("userHandle") val userHandle: String
    )
}

fun handlePasskeyRegistrationResult(
    result: CreateCredentialResponse,
    myRef: DatabaseReference,
    user: String
) {
    myRef.child("Users").child(user).child("Passkey").push().setValue(result)
}

fun fetchRegistrationJsonFromServer(context: Context,name:String): String {
    val response = context.readFromAsset("RegFromServer")

    //Update userId, name and Display name in the mock
    return response.replace("<userId>", getEncodedUserId())
        .replace("<userName>", name)
        .replace("<userDisplayName>", name)
        .replace("<challenge>", getEncodedChallenge())
}

fun Context.readFromAsset(fileName: String): String {
    var data = ""
    this.assets.open(fileName).bufferedReader().use {
        data = it.readText()
    }
    return data
}

fun getEncodedUserId(): String {
    val random = SecureRandom()
    val bytes = ByteArray(64)
    random.nextBytes(bytes)
    return Base64.encodeToString(
        bytes,
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
    )
}

private fun getEncodedChallenge(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return Base64.encodeToString(
        bytes,
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
    )
}

fun fetchAuthJsonFromServer(context: Context): String {
    return context.readFromAsset("AuthFromServer")
}
