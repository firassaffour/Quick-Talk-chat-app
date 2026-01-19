package com.example.quicktalkcompose.call_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.domain.models.CallsItem
import com.example.quicktalkcompose.ui.theme.QuickTalkComposeTheme
import com.example.quicktalkcompose.ui.theme.grayText

@Composable
fun CallsScreen(){

    val callList = listOf(CallsItem())
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(text = "Calls",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp)

        Spacer(Modifier.height(20.dp))

        LazyColumn {
            items(callList){ call ->
                Row( modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberAsyncImagePainter(call.userProfileImage),
                        contentDescription = "profileImage",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                shape = CircleShape,
                                color = Color.Black
                            )
                    )

                    Spacer(Modifier.width(10.dp))

                    Column( modifier = Modifier.weight(3f)) {
                        Text(
                            text = call.userName,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = call.callDate,
                            color = grayText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } // Column

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Call,
                            contentDescription = "call",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                } // Row
            } // items
        } // LazyColumn
    }
}

@Composable
@Preview(showBackground = true)
fun CallsScreenPreview(){
    QuickTalkComposeTheme {
        CallsScreen()
    }
}