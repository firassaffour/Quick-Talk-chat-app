package com.example.quicktalkcompose.signup_screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.domain.models.BottomNavItem
import com.example.quicktalkcompose.isDark
import com.example.quicktalkcompose.ui.theme.Gradient
import com.example.quicktalkcompose.ui.theme.QuickTalkComposeTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun SignUpScreen(navController: NavHostController, signUpViewModel: SignUpViewModel? = null){
    val auth = FirebaseAuth.getInstance()
    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberCode by remember { mutableStateOf("") }
    var phoneNumberWithCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            if (isCodeSent) isCodeSent = false
             else navController.popBackStack()}) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "backButton",
                tint = MaterialTheme.colorScheme.onBackground
            )
        } // IconButton
    } // Row

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.height(50.dp))

        Image(
            painter = if (isDark) painterResource(id = R.drawable.quicktallklightlogo)
                else painterResource(id = R.drawable.quicktallkdarklogo),
            contentDescription = "logo",
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(30.dp))


        if (!isCodeSent){
            Text(
                text = "Enter your Phone Number",
                textAlign = TextAlign.Center,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier
                        .weight(2f),
                    value = phoneNumberCode,
                    onValueChange = {
                        phoneNumberCode = if (it.length >= 4) phoneNumberCode
                        else it
                    },
                    maxLines = 1,
                    placeholder = { Text("1", color = Color(130,130,130,255)) },
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                    leadingIcon = { Icon(Icons.Default.Add ,
                        contentDescription = "plus",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(15.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    modifier = Modifier
                        .weight(5f)
                        .padding(start = 8.dp),
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = if (it.length >= 14) phoneNumber
                        else it
                    },
                    maxLines = 1,
                    placeholder = { Text("Phone Number", color = Color(130,130,130,255)) },
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(30.dp))

            Button(onClick = {
                isLoading = true
                if (phoneNumber.isEmpty()){
                    isLoading = false
                    Toast.makeText(context, "please write your phone number", Toast.LENGTH_SHORT).show()
                }
                if (phoneNumberCode.isEmpty()){
                    isLoading = false
                    Toast.makeText(context, "please write your local code number", Toast.LENGTH_SHORT).show()
                }

                else {
                    phoneNumberWithCode = "+$phoneNumberCode$phoneNumber"
                    val option = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                            override fun onVerificationCompleted(credentail: PhoneAuthCredential) {
                                auth.signInWithCredential(credentail).addOnCompleteListener {
                                    message = if (it.isSuccessful) "successful"
                                    else "failed"
                                    isLoading = false
                                }
                            }

                            override fun onVerificationFailed(p0: FirebaseException) {
                                message = "failed"
                                isLoading = false
                            }

                            override fun onCodeSent(id: String, p1: PhoneAuthProvider.ForceResendingToken) {
                                verificationId = id
                                isCodeSent = true
                                isLoading = false
                            }
                        }).build()
                    PhoneAuthProvider.verifyPhoneNumber(option)
                }
            },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                enabled = !isLoading
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gradient),
                    contentAlignment = Alignment.Center){
                    if (isLoading){
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            color = Color.White
                        )
                    }
                    else Text(text = "Next", color = Color.White, fontSize = 18.sp)
                } // Box
            } // Button
        } // if

        else {
            Text(
                text = "Enter the Code",
                textAlign = TextAlign.Center,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    value = verificationCode,
                    onValueChange = {
                        verificationCode = if (it.length >= 7) verificationCode
                        else it
                    },
                    placeholder = { Text("OTP Code", color = Color(130, 130, 130, 255)) },
                    label = { Text("OTP", color = Color(130, 130, 130, 255)) },
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    isLoading = true
                    if (verificationCode.isEmpty()){
                        isLoading = false
                        Toast.makeText(context, "please write the OTP code", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
                        auth.signInWithCredential(credential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user = it.result?.user
                                if (user != null){
                                    message =  "successful"
                                    isLoading = false
                                    phoneNumberWithCode = "+$phoneNumberCode$phoneNumber"

                                    signUpViewModel?.signup(phoneNumberWithCode,
                                        context,
                                        firstSign = {navController.navigate(AppScreens.FirstSignUp.route)},
                                        alreadyExist = {navController.navigate(BottomNavItem.Home.route) {popUpTo(0) {inclusive = true} }})
                                }

                            }
                            else{
                                message = "failed"
                                isLoading = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                enabled = !isLoading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gradient),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading){
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            color = Color.White
                        )
                    }
                    else Text(text = "Next", color = Color.White, fontSize = 18.sp)
                } // Box
            } // Button
        }
    } // Column
}

@Composable
@Preview(showBackground = true)
fun SignUpScreenPreview(){
    QuickTalkComposeTheme {
        val nav = rememberNavController()
        SignUpScreen(nav)
    }
}