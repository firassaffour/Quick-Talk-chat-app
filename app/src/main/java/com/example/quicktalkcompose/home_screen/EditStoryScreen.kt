package com.example.quicktalkcompose.home_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue

@Composable
fun EditStoryScreen(navController: NavHostController, homeViewModel: HomeViewModel){

    val messageText by homeViewModel.messageText.collectAsState()

    Column(modifier = Modifier.padding(8.dp)) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(60.dp))

        Image(
            rememberAsyncImagePainter(homeViewModel.selectedStoryImageUri),
            contentDescription = "story",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .weight(5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(5f),
                    value = messageText,
                    onValueChange = { homeViewModel.updateMessageText(it) },
                    placeholder = { Text("Reply", color = grayText) },
                    shape = RoundedCornerShape(32.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            } // Row

            Spacer(Modifier.width(5.dp))

                IconButton(
                    onClick = {
                        homeViewModel.uploadStory(homeViewModel.selectedStoryImageUri!!, messageText)
                        homeViewModel.updateMessageText("")
                        navController.popBackStack()
                        homeViewModel.setShouldAnimate(true)
                    }, Modifier
                        .background(color = primBlue, shape = CircleShape)
                        .weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "upload",
                        tint = MaterialTheme.colorScheme.background
                    )
                } // IconButton
        } // Row
    } // Column
}

@Composable
@Preview(showBackground = true)
fun EditStoryScreenPreview(){
}