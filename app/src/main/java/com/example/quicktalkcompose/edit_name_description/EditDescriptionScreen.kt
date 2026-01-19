package com.example.quicktalkcompose.edit_name_description

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


@Composable
fun EditDescriptionScreen(navController: NavHostController, previousDescription : String){
    var description by remember { mutableStateOf(previousDescription) }
    var descriptionCount by remember { mutableStateOf(description.length) }
    var isCanceling by remember { mutableStateOf(false) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid!!)

    val descriptionList = listOf("Available", "Busy", "at school", "at the movie", "at work", "at the gym", "sleeping", "Argent calls only")

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                if (description != previousDescription){
                    isCanceling = true
                }
                else navController.popBackStack()}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "backButton",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            } // IconButton

            Spacer(Modifier.width(30.dp))

            Text(
                text = "Description",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        } // Row

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            value = description,
            onValueChange = {
                if (it.length >= 151) description = description
                else {
                    description = it
                    descriptionCount = description.length
                } },
            placeholder = { Text("Description", color = Color(130, 130, 130, 255)) },
            label = { Text("Your Description", color = Color(130, 130, 130, 255)) },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Text(
            text = "$descriptionCount/150",
            color = grayText,
            modifier = Modifier
                .align(Alignment.End)
                .padding(8.dp)
        )

        Text(
            text = "select description",
            color = grayText,
            modifier = Modifier
                .padding(8.dp)
        )

        LazyColumn {
            items(descriptionList){ descriptionItem ->
                Row(modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        description = descriptionItem
                    descriptionCount = description.length}) {
                    Text(
                        text = descriptionItem,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 19.sp
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                navController.popBackStack()
                userRef.child("description").setValue(description)
            },
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            shape = RoundedCornerShape(2.dp),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(primBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Save", color = Color.White, fontSize = 18.sp)
            } // Box
        } // Button
    }
    if (isCanceling){
        AlertDialog(
            onDismissRequest = {isCanceling = false},
            title = { Text("Cancel Editing Description" , color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("are you sure you want to cancel without saving ?") },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                TextButton(onClick = {
                    navController.popBackStack()
                    isCanceling = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { isCanceling = false}) { Text("No") }
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun EditDescriptionScreenPreview(){
    val nav = rememberNavController()
    EditDescriptionScreen(nav, "")
}