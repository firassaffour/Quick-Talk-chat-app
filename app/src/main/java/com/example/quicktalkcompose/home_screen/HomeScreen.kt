package com.example.quicktalkcompose.home_screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer


@Composable
fun HomeScreen(navController : NavHostController, homeViewModel: HomeViewModel) {
    val usersList by homeViewModel.usersList.collectAsState()
    val storiesList by homeViewModel.storiesList.collectAsState()
    val myStory by homeViewModel.myStory.collectAsState()
    val shouldAnimate by homeViewModel.shouldAnimate.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var showDropDownMenu by remember { mutableStateOf(false) }
    var showBorderAnimation by remember { mutableStateOf(false) }
    val storyBorderRotation = remember { Animatable(0f) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val currentRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid!!)
    var currentName by remember { mutableStateOf("") }
    var currentImage by remember { mutableStateOf("") }
        currentRef.get().addOnSuccessListener { snapShot ->
        currentName = snapShot.child("name").getValue(String::class.java) ?: ""
        currentImage = snapShot.child("profileImage").getValue(String::class.java) ?: ""
    }

    // story adding
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri ->
            homeViewModel.selectedStoryImageUri = imageUri
            navController.navigate(AppScreens.EditStory.route)

        }
    }

    // story border animation
    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate){
            showBorderAnimation = true
            storyBorderRotation.animateTo(targetValue = 360f, animationSpec = tween(durationMillis = 3700, easing = LinearEasing))
            storyBorderRotation.snapTo(0f)
            homeViewModel.setShouldAnimate(false)
            showBorderAnimation = false
        }
    }

    LaunchedEffect(currentUid) {
        homeViewModel.getUsersChat()

        homeViewModel.getAllUsersStories()

        homeViewModel.getUserStories(currentUid)

        isLoading = false
    }


    Box {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(color = MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QuickTalk",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { showDropDownMenu = !showDropDownMenu }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "menu",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
                )

                DropdownMenu(
                    expanded = showDropDownMenu,
                    onDismissRequest = { showDropDownMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings", color = MaterialTheme.colorScheme.onBackground) },
                        onClick = {
                            showDropDownMenu = false
                            navController.navigate(AppScreens.Settings.route)
                        }
                    )
                } // DropDownMenu
            } // IconButton
        } // Row

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = primBlue,
                )
            }
        }
        else{
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(64.dp)
                                .clickable {
                                    if (myStory.stories.isNotEmpty()) navController.navigate(
                                        "${AppScreens.StoryViewer.route}/$currentUid"
                                    )
                                },
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        color = when {
                                            myStory.stories.isEmpty() -> MaterialTheme.colorScheme.background
                                            !shouldAnimate && !myStory.isSeen -> primBlue
                                            !shouldAnimate && myStory.isSeen -> grayText
                                            else -> MaterialTheme.colorScheme.background
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (showBorderAnimation) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                rotationZ = storyBorderRotation.value
                                            }) {
                                        drawArc(
                                            color = primBlue,
                                            startAngle = 0f,
                                            sweepAngle = 270f,
                                            useCenter = false,
                                            style = Stroke(
                                                width = 6.dp.toPx(),
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }

                                Image(
                                    painter = rememberAsyncImagePainter(currentImage),
                                    contentDescription = "profileImage",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                )
                            } // Box
                        } // Card
                        IconButton(
                            onClick = {launcher.launch("image/*")},
                            colors = IconButtonDefaults.iconButtonColors(containerColor = primBlue),
                            modifier = Modifier
                                .offset(y = (-20).dp)
                                .size(20.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } // Column
                } // item

                itemsIndexed(storiesList) { index, story ->

                    var isVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(index * 100L)
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -300 },
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(64.dp)
                                    .clickable { navController.navigate("${AppScreens.StoryViewer.route}/${story.userId}") },
                                shape = CircleShape,
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(color = if (story.isSeen) grayText else primBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(story.userProfileImage),
                                        contentDescription = "profileImage",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(CircleShape)
                                    )
                                } // Box
                            } // Card
                            Text(
                                text = story.userName,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                        } // Column
                    } //AnimatedVisibility
                } // Items
            } // LazyRow

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            Modifier
                .fillMaxWidth()
        ) {
            itemsIndexed(usersList) { index, user ->
                var isVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(index * 100L)
                    isVisible = true
                }
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -300 },
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeIn()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        modifier = Modifier
                            .clickable { navController.navigate("${AppScreens.Messaging.route}/${user.userId}") }
                            .padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(64.dp)) {
                                Image(
                                    rememberAsyncImagePainter(user.profileImage),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 1.dp,
                                            shape = CircleShape,
                                            color = Color.Black
                                        )
                                        .clickable { }
                                )
                                if (user.isOnline){
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(3f)) {
                                Text(
                                    text = user.name,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = user.lastMessage,
                                    color = grayText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            } // Column

                            Spacer(Modifier.weight(1f))

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = homeViewModel.formatTime(user.lastMessageTime),
                                    color = if (user.unreadCount == 0) grayText else primBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(5.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (user.isMuted){
                                        Icon(
                                            painter = painterResource(id = R.drawable.notifications_off),
                                            contentDescription = "bell",
                                            tint = grayText
                                        )

                                        Spacer(Modifier.width(5.dp))
                                    }

                                    if (user.unreadCount != 0){
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = primBlue,
                                                    shape = CircleShape
                                                )
                                                .size(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (user.unreadCount >= 100) "+99"
                                                       else "${user.unreadCount}",
                                                color = Color.White,
                                                fontSize = if (user.unreadCount >= 10) 10.sp
                                                else 14.sp
                                            )
                                        }
                                    }
                                } // Row
                            } // Column
                        } // Row
                    } // Card
                } // AnimatedVisibility
            } // Items
        } // LazyColumn
    }
    } // Column
        FloatingActionButton(
            onClick = {navController.navigate(AppScreens.FindContacts.route)},
            containerColor = primBlue,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd)
        ) {

            Icon(
                painter = painterResource(id = R.drawable.add_content),
                contentDescription = "message",
                tint = Color.White
            )
        }
} //Box
} // HomeScreen

@Composable
@Preview(showBackground = true)
fun HomeScreenPreview(){
}