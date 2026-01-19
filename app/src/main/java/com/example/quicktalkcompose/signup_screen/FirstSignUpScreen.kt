package com.example.quicktalkcompose.signup_screen

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.BottomNavItem
import com.example.quicktalkcompose.ui.theme.QuickTalkComposeTheme
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun FirstSignUpScreen(navController: NavHostController){
    var isLoading by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val profileImage by remember { mutableStateOf("") }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid!!)
    val context = LocalContext.current
    val signUpViewModel : SignUpViewModel = viewModel(factory =
        ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application))
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            isLoading = true
            selectedImageUri = uri
            signUpViewModel.saveProfileImageUrlToFirebase(it)
            isLoading = false
        }
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Text(text = "Create your profile",
            color = primBlue,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(40.dp))

        Box(modifier = Modifier
            .background(color = grayText, shape = CircleShape)
            .size(120.dp)
            .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center){
            if (isLoading){
                CircularProgressIndicator(
                    color = Color.White
                )
            }
            else {
                Image(
                    painter = if (profileImage.isEmpty()) painterResource(id = R.drawable.camera_icon)
                    else rememberAsyncImagePainter(profileImage),
                    contentDescription = "profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(if (profileImage.isEmpty()) 50.dp else 120.dp)
                        .clickable { launcher.launch("image/*") }
                )
            }
        }

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Name", color = Color(130,130,130,255)) },
            label = { Text("Name", color = Color(130,130,130,255)) },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedIndicatorColor = primBlue),
            leadingIcon = { Icon(imageVector = Icons.Default.Person,
                contentDescription = "name",
                tint = Color(130,130,130,255)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Description", color = Color(130,130,130,255)) },
            label = { Text("Description", color = Color(130,130,130,255)) },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedIndicatorColor = primBlue),
            leadingIcon = { Icon(painter = painterResource(id = R.drawable.about_icon),
                contentDescription = "description",
                tint = Color(130,130,130,255)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(Modifier.weight(1f))

        Button(onClick = {
            if (name.isNotEmpty()){
                userRef.child("name").setValue(name)

                if (profileImage.isNotEmpty()) userRef.child("profileImage").setValue(profileImage)
                else userRef.child("profileImage").setValue("https://res.cloudinary.com/dkiblm397/image/upload/v1759407441/profileIcon_wkyat3.png")

                if (description.isNotEmpty()) userRef.child("description").setValue(description)
                navController.navigate(BottomNavItem.Home.route){popUpTo(0) {inclusive = true} }
            }
            else Toast.makeText(context, "Please write your name", Toast.LENGTH_SHORT).show()
        },
            modifier = Modifier
                .width(100.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primBlue
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Done",
                color = Color.White)
        }

    }
}


@Composable
@Preview(showBackground = true)
fun FirstSignUpScreenPreview(){
    QuickTalkComposeTheme {
        val nav = rememberNavController()
        FirstSignUpScreen(nav)
    }
}