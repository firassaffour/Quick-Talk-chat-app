package com.example.quicktalkcompose.home_screen

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.quicktalkcompose.domain.models.Story
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoriesViewer(navController: NavHostController, story: Story, homeViewModel: HomeViewModel){

    val stories = story.stories
    val messageText by homeViewModel.messageText.collectAsState()
    val chatState by homeViewModel.chatState.collectAsState()
    val currentUser = homeViewModel.getCurrentInfo()
    var isPaused by remember { mutableStateOf(false) }

    // story indicator
    var currentIndex by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    val coroutine = rememberCoroutineScope()

    // setting the indicator animation
    LaunchedEffect(currentIndex) {
        progress.snapTo(0f)
        while (progress.value < 1f){
            if (!isPaused){
                homeViewModel.setStoriesSeen(story.userId, stories[currentIndex].storyId)
                progress.snapTo(progress.value + 0.005f)
            }
            delay(25)
        }
        if (currentIndex < stories.size - 1){
            currentIndex ++
        }
        else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(story.userId) {
        homeViewModel.observeChatState(currentUser.userId, story.userId)
    }

    Column(modifier = Modifier
        .fillMaxSize()) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            stories.forEachIndexed { index, _ ->
                LinearProgressIndicator(
                    progress = when {
                        index < currentIndex -> 1f
                        index == currentIndex -> progress.value
                        else -> 0f
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                )
            }
        } // Row

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {navController.popBackStack()},
                modifier = Modifier
                    .padding(top = 8.dp)) {
                Icon(imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back",
                    tint = MaterialTheme.colorScheme.onBackground)
            }

            Image(
                painter = rememberAsyncImagePainter(story.userProfileImage),
                contentDescription = "profileImage",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .padding(8.dp)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = story.userName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                )

                Text(
                    text = story.stories[currentIndex].time,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            } // Column
        } // Row

        Spacer(Modifier.height(60.dp))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(stories[currentIndex].imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "stories",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .pointerInput(Unit){
                    detectTapGestures(
                        onLongPress = {isPaused = true},
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            coroutine.launch {
                                val width = size.width
                                if (offset.x < width / 2){
                                    if (currentIndex > 0){
                                        currentIndex --
                                        progress.snapTo(0f)
                                    }
                                }
                                else {
                                    if (currentIndex < stories.size - 1){
                                        currentIndex ++
                                        progress.snapTo(0f)
                                    }
                                    else navController.popBackStack()
                                }
                            }
                        }
                    )
                }
        )

        Spacer(Modifier.height(50.dp))

        Text(
            text = stories[currentIndex].text,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 19.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        if (story.userId != currentUser.userId) {
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
                            .weight(5f)
                            .onFocusChanged { isPaused = it.isFocused },
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

                if (messageText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val status = if (chatState.receiverIsInChat) "Seen" else "Sent"
                            val storyText = stories[currentIndex].text
                            homeViewModel.sendMessage(
                                receiverUid = story.userId,
                                messageText = messageText,
                                status = status,
                                storyReply = true,
                                storyText = storyText,
                                storyImage = stories[currentIndex].imageUrl
                            ) {}
                            if (!chatState.currentIsMuted) {
                                if (!chatState.receiverIsInChat) {
                                    homeViewModel.sendMessageNotification(story.userId, messageText)
                                }
                            }
                            homeViewModel.updateMessageText("")
                        }, Modifier
                            .background(color = primBlue, shape = CircleShape)
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "send",
                            tint = MaterialTheme.colorScheme.background
                        )
                    } // IconButton
                } // if
            } // Row
        } // if
    } // Column
}

@Composable
@Preview(showBackground = true)
fun StoriesViewerPreview(){

}