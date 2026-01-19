package com.example.quicktalkcompose.settings_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.signup_screen.SignUpViewModel
import com.example.quicktalkcompose.isDark
import com.example.quicktalkcompose.ui.theme.QuickTalkComposeTheme
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun SettingsScreen(navController : NavHostController, signUpViewModel: SignUpViewModel) {
    var isLoading by remember { mutableStateOf(true) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid!!)
    var profileImage by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            isLoading = true
            selectedImageUri = uri
            signUpViewModel.saveProfileImageUrlToFirebase(it)
            isLoading = false
        }
    }

    // getting current user data
    LaunchedEffect(Unit) {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                name = snapshot.child("name").getValue(String::class.java) ?: ""
                description = snapshot.child("description").getValue(String::class.java) ?: ""
                profileImage = snapshot.child("profileImage").getValue(String::class.java) ?: ""
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    } // LaunchEffect

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {navController.popBackStack()}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "backButton",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            } // IconButton

            Spacer(Modifier.width(30.dp))

            Text(
                text = "Settings",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        } // Row

        Spacer(Modifier.height(20.dp))

        if (isLoading){
            Box(modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center){
                CircularProgressIndicator(
                    color = primBlue,
                )
            }
        }

        else{
            Box(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(130.dp)
                .background(color = primBlue, shape = CircleShape),
            contentAlignment = Alignment.Center){

                Image(painter = if (profileImage.isEmpty())painterResource(id = R.drawable.profileicon)
                else rememberAsyncImagePainter(profileImage),
                    contentDescription = "profileImage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(130.dp)
                        .clickable {  }
                        .clip(shape = CircleShape)
                )

                IconButton(onClick = {launcher.launch("image/*")},
                    colors = IconButtonDefaults.iconButtonColors(containerColor = primBlue),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp, y = 40.dp)) {
                    Icon(painter = painterResource(id = R.drawable.camera_icon),
                        contentDescription = "camera",
                        tint = Color.White)
                }
            } // Box

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("${AppScreens.EditName.route}/${name}")
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = " Name",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = " $name",
                        fontSize = 18.sp,
                        color = grayText
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("${AppScreens.EditDescription.route}/${description}")
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.about_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = " Description",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = " $description",
                        fontSize = 18.sp,
                        color = grayText
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.dark_mode),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = " Dark Mode",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )

                Switch(
                    checked = isDark,
                    onCheckedChange = { isDark = !isDark },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = primBlue
                    )
                )
            } // Row

            Button(modifier = Modifier
                .fillMaxWidth(),
                onClick = {
                    navController.navigate(AppScreens.SignUp.route)
                    signUpViewModel.signOut()},
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Logout",
                    color = Color.Black,)
            }
        }
    } // Column
}

@Composable
@Preview(showBackground = true)
fun SettingsScreenPreview(){
    QuickTalkComposeTheme {
    }
}