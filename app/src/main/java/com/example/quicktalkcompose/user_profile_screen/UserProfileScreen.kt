package com.example.quicktalkcompose.user_profile_screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.User
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue

@Composable
fun UserProfileScreen(navController: NavHostController, user: User) {

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {

        Column(modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(1.dp))
            .fillMaxWidth()) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "backButton",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                } // IconButton
            } // Row

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(130.dp)
                    .background(color = primBlue, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = rememberAsyncImagePainter(user.profileImage),
                    contentDescription = "profileImage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(130.dp)
                        .clickable { }
                        .clip(shape = CircleShape)
                )

                if (user.isOnline){
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
            }

            Text(
                text = user.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 19.sp,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = user.phoneNumber,
                color = grayText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(Modifier.weight(1f))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(width = 1.dp, color = grayText),
                    modifier = Modifier
                        .padding(8.dp)
                        .width(120.dp)
                        .height(70.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.message_icon),
                            contentDescription = "message",
                            tint = primBlue,
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            text = "Message",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 19.sp
                        )
                    } // Column
                } // Card

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(width = 1.dp, color = grayText),
                    modifier = Modifier
                        .padding(8.dp)
                        .width(120.dp)
                        .height(70.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "calls",
                            tint = primBlue,
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            text = "Call",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 19.sp
                        )
                    } // Column
                } // Card

                Spacer(Modifier.weight(1f))

            } // Row
        } // Column

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(1.dp))
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = user.description,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 19.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(1.dp))
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {

                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.block_icon),
                contentDescription = null,
                tint = Color.Red
            )
                Text(
                    text = " Block ${user.name}",
                    fontSize = 19.sp,
                    color = Color.Red
                )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(1.dp))
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {

                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.report_icon),
                contentDescription = null,
                tint = Color.Red
            )
            Text(
                text = " Report ${user.name}",
                fontSize = 19.sp,
                color = Color.Red
            )
        }

    }
}

@Composable
@Preview(showBackground = true)
fun UserProfileScreenPreview(){
    val nav = rememberNavController()
}