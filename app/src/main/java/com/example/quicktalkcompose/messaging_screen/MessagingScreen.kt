package com.example.quicktalkcompose.messaging_screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.domain.models.Message
import com.example.quicktalkcompose.domain.models.User
import com.example.quicktalkcompose.isDark
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MessagingScreen(navController: NavHostController, user: User, messagingViewModel: MessagingViewModel){
    // screen general
    var showDropDownMenu by remember { mutableStateOf(false) }
    val messageText by messagingViewModel.messageText.collectAsState()
    val chatState by messagingViewModel.chatState.collectAsState()
    val chatMessages by messagingViewModel.chatMessages.collectAsState()
    val listState = rememberLazyListState()
    var showOptions by remember { mutableStateOf(false) }
    var pressedMessage by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    var firstLoad by remember { mutableStateOf(true) }
    var isReplying by remember { mutableStateOf(false) }
    var repliedMessage by remember { mutableStateOf(Message()) }
    var isEditing by remember { mutableStateOf(false) }
    var editedMessage by remember { mutableStateOf(Message()) }

    // when new message is received you scroll only if your screen is on the last message
    val shouldAutoScroll by remember { derivedStateOf {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        lastVisible >= listState.layoutInfo.totalItemsCount - 2
    } }

    // auto scroll when new message is received
    LaunchedEffect(chatMessages.size, chatState.receiverIsTyping, shouldAutoScroll) {
        if (firstLoad && chatMessages.isNotEmpty()) {
            listState.scrollToItem(chatMessages.size - 1)
            firstLoad = false
        }
        if (shouldAutoScroll) {
            val lastIndex = chatMessages.size + if (chatState.receiverIsTyping) 1 else 0
            if (lastIndex > 0) listState.animateScrollToItem(lastIndex - 1)
        }
    }

    LaunchedEffect(chatMessages.lastOrNull()?.reactionCount) {
        snapshotFlow { chatMessages.size }
            .collect { delay(100) }
        listState.animateScrollToItem(chatMessages.size - 1)
    }

    // typing design
    val typingDots = listOf(".", "..", "...")
    var currentTypingIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true){
            delay(500)
            currentTypingIndex = (currentTypingIndex + 1) % typingDots.size
        }
    }

    // image selection
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageIsSelected by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = uri
            imageIsSelected = true
        }
    }

    // current information
    val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
    val receiverUid = user.userId
    val currentUser = messagingViewModel.getCurrentInfo()

    // getting chat states
    LaunchedEffect(receiverUid) {
        messagingViewModel.observeChatState(currentUid, receiverUid)
    }

    // setting if current user is typing or not
    LaunchedEffect(messageText) {
        messagingViewModel.setCurrentIsTyping(currentUid, receiverUid)
    }

    // add new message delete or edit messages
    LaunchedEffect(user.userId) {
        messagingViewModel.listenForMessages(user.userId)
    }

    // setting if current user is in chat
    LaunchedEffect(currentUid) {
        messagingViewModel.setCurrentIsInChat(currentUid, receiverUid, false)
    }
    DisposableEffect(Unit) {
        onDispose { messagingViewModel.setCurrentIsInChat(currentUid, receiverUid, true) }
    }

    // setting UnRead messages and messages status
    LaunchedEffect(Unit) {
        messagingViewModel.setMessagesStatusToRead(currentUid, receiverUid)
    }

    // Messaging Screen UI
    Column(modifier = Modifier
        .fillMaxSize()
        .paint(
            if (isDark) painterResource(id = R.drawable.darkmessagebackground) else painterResource(
                id = R.drawable.lightmessagesbackground
            ),
            contentScale = ContentScale.FillBounds
        ),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(color = MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {navController.popBackStack()}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "backButton",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            } // IconButton

            Spacer(Modifier.width(10.dp))

            Box(Modifier.size(45.dp)) {
                Image(
                    if (user.profileImage == "") painterResource(id = R.drawable.profileicon)
                    else rememberAsyncImagePainter(user.profileImage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            shape = CircleShape,
                            color = Color.Black
                        )
                        .clickable { navController.navigate("${AppScreens.UserProfile.route}/${user.userId}") }
                )
                if (user.isOnline){
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Text(
                text = user.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("${AppScreens.UserProfile.route}/${user.userId}") }
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
                        text = { Text(if (chatState.receiverIsMuted) "UnMute" else "Mute",
                            color = MaterialTheme.colorScheme.onBackground) },
                        onClick = {
                            showDropDownMenu = false
                            messagingViewModel.muteTheUser(currentUid, receiverUid, chatState.receiverIsMuted)
                        }
                    )
                } // DropDownMenu
            } // IconButton
        } // Row

        Spacer(Modifier.height(20.dp))

        LazyColumn(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
            state = listState) {
            items(chatMessages){ message ->
                val offsetX = remember { Animatable(0f) }
                val threshold = 250f
                val coroutineScope = rememberCoroutineScope()
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                    horizontalArrangement = if(message.senderId == currentUid) Arrangement.End else Arrangement.Start ) {
                    Column {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp, min = 70.dp)
                                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            showOptions = true
                                            pressedMessage = message.messageId
                                        },
                                        onDoubleTap = {
                                            messagingViewModel.setReactionToMessage(
                                                currentUid,
                                                receiverUid,
                                                message.messageId,
                                                message.messageText
                                            )
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    if (message.senderId == receiverUid) {
                                        detectHorizontalDragGestures(
                                            onDragEnd = {
                                                coroutineScope.launch {
                                                    if (offsetX.value < threshold) {
                                                        offsetX.animateTo(
                                                            0f,
                                                            animationSpec = tween(300)
                                                        )
                                                    }
                                                }
                                            },
                                            onHorizontalDrag = { _, dragAmount ->
                                                coroutineScope.launch {
                                                    var newOffset = offsetX.value + dragAmount
                                                    if (newOffset < 0) newOffset = 0F
                                                    offsetX.snapTo(newOffset)

                                                    if (newOffset > threshold) {
                                                        repliedMessage = message
                                                        isReplying = true
                                                        offsetX.animateTo(
                                                            0f,
                                                            animationSpec = tween(300)
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                },
                            elevation = CardDefaults.cardElevation(6.dp),
                            colors = CardDefaults.cardColors(containerColor = if(message.senderId == currentUid) primBlue else grayText),
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                if (message.repliedTo != null){
                                    Card(modifier = Modifier
                                        .fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(27,27,27,255))) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(text = if (message.repliedTo.senderId == currentUid) currentUser.name else user.name,
                                                color = primBlue)

                                            Text(text = message.repliedTo.messageText,
                                                color = grayText,
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1)
                                        }
                                    }
                                }

                                if(message.image.isNotEmpty()){
                                    Image(
                                        painter = rememberAsyncImagePainter(message.image),
                                        contentDescription = "messageImage",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                    )
                                }

                                if (message.storyReply){
                                    Card(modifier = Modifier
                                        .fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(27,27,27,255))) {
                                        Row(modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically) {

                                            Image(
                                                painter = rememberAsyncImagePainter(message.storyImage),
                                                contentDescription = "profileImage",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .padding(8.dp)
                                            )

                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = user.name,
                                                    color = primBlue
                                                )

                                                Text(
                                                    text = message.storyText,
                                                    color = grayText,
                                                    overflow = TextOverflow.Ellipsis,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = message.messageText,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                                Row(modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    if (message.isEdited){
                                        Text(
                                            text = "Edited",
                                            color = Color.LightGray ,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }

                                    Text(
                                        messagingViewModel.formatTime(message.messageTime),
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                    )

                                    if (message.senderId == currentUid){
                                        Text(
                                            text = message.status,
                                            fontSize = 11.sp,
                                            color = if (message.status == "Seen") Color.Green else grayText
                                        )
                                    }
                                }
                            } // Column
                            if (message.messageId == pressedMessage){
                                DropdownMenu(
                                    expanded = showOptions,
                                    onDismissRequest = { showOptions = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Copy", color = MaterialTheme.colorScheme.onBackground) },
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(message.messageText))
                                            showOptions = false
                                        }
                                    )
                                    if (message.senderId == currentUid) {
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = MaterialTheme.colorScheme.onBackground) },
                                            onClick = {
                                                messagingViewModel.deleteMessage(currentUid, receiverUid, message.messageId)
                                                showOptions = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Reply", color = MaterialTheme.colorScheme.onBackground) },
                                            onClick = {
                                                repliedMessage = message
                                                isReplying = true
                                                showOptions = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Edit", color = MaterialTheme.colorScheme.onBackground) },
                                            onClick = {
                                                editedMessage = message
                                                editedMessage.messageText = message.messageText
                                                messagingViewModel.updateMessageText(message.messageText)
                                                isReplying = false
                                                isEditing = true
                                                showOptions = false
                                            }
                                        )
                                    } // if
                                } // DropDownMenu
                            } // if
                        } // Card
                        if (message.reactionCount != 0 ){
                            Row(modifier = Modifier
                                .width(70.dp)
                                .padding(start = 8.dp)
                                .offset(y = (-3).dp)) {
                                Box(Modifier
                                    .background(
                                        color = grayText,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 4.dp)){
                                    Text(text = if (message.reactionCount == 1) "❤" else "${message.reactionCount} ❤" ,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 14.sp)
                                }
                            }
                        }
                    } // column
                } // Row
            } // items
            if (chatState.receiverIsTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column {
                            Card(
                                modifier = Modifier
                                    .widthIn(max = 70.dp, min = 70.dp),
                                elevation = CardDefaults.cardElevation(6.dp),
                                colors = CardDefaults.cardColors(containerColor = grayText)
                            ) {
                                Text(
                                    text = typingDots[currentTypingIndex],
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            } // Card
                        } // Column
                    } // Row
                } // item
            } // if
        } // LazyColumn

        if (isReplying){
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(27,27,27,255))) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (repliedMessage.senderId == currentUid) currentUser.name else user.name,
                            color = primBlue)

                        Spacer(Modifier.weight(1f))

                        IconButton(onClick = {isReplying = false}) {
                            Icon(imageVector = Icons.Default.Clear,
                                contentDescription = "x",
                                tint = Color.White)
                        }
                    }
                    Text(text = repliedMessage.messageText,
                        color = grayText,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1)
                }
            }
        }

        if (isEditing){
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(27,27,27,255))) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentUser.name , color = primBlue)

                        Spacer(Modifier.weight(1f))

                        IconButton(onClick = {
                            isEditing = false
                            messagingViewModel.updateMessageText("")}) {
                            Icon(imageVector = Icons.Default.Clear,
                                contentDescription = "x",
                                tint = Color.White)
                        }
                    }
                    Text(text = editedMessage.messageText,
                        color = grayText,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1)
                }
            }
        }

        if (imageIsSelected){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center){
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "image",
                        Modifier.size(100.dp)
                    )

                    IconButton(onClick = {
                        imageIsSelected = false
                        selectedImageUri = null
                    },
                        modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "x",
                            tint = Color.Black
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            } // Row
        } // if
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
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
                    onValueChange = { messagingViewModel.updateMessageText(it) },
                    placeholder = { Text("Message", color = grayText) },
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

                IconButton(onClick = {launcher.launch("image/*")},
                    Modifier.weight(1f)){
                    Icon(
                        painter = painterResource(id = R.drawable.image_icon),
                        contentDescription = "send",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } // Row

            Spacer(Modifier.width(5.dp))

            if (messageText.isNotEmpty() || imageIsSelected) {
                IconButton(onClick = {
                    when {
                        !isReplying && !isEditing && !imageIsSelected ->{
                            val status = if (chatState.receiverIsInChat) "Seen" else "Sent"
                            messagingViewModel.sendMessage(receiverUid, messageText, status = status){}
                        }

                        isEditing -> {
                            messagingViewModel.editMessage(receiverUid, messageText, editedMessage.messageId)
                            isEditing = false
                        }

                        isReplying && !imageIsSelected -> {
                            val status = if (chatState.receiverIsInChat) "Seen" else "Sent"
                            messagingViewModel.sendMessage(receiverUid, messageText, repliedTo = repliedMessage, status = status){}
                            isReplying = false
                        }

                        imageIsSelected && !isReplying -> {
                            val status = if (chatState.receiverIsInChat) "Seen" else "Sent"
                            messagingViewModel.sendMessage(receiverUid, messageText, imageUri = selectedImageUri!!, status = status){
                                imageIsSelected = false
                            }
                        }

                        imageIsSelected && isReplying -> {
                            val status = if (chatState.receiverIsInChat) "Seen" else "Sent"
                            messagingViewModel.sendMessage(receiverUid, messageText, repliedTo = repliedMessage, imageUri = selectedImageUri!!, status = status){
                                imageIsSelected = false
                                isReplying= false
                            }
                        }
                    }
                    if (!chatState.currentIsMuted) {
                        if (!chatState.receiverIsInChat){
                            messagingViewModel.sendMessageNotification(receiverUid, messageText)
                        }
                    }
                    messagingViewModel.updateMessageText("")
                }, Modifier
                    .background(color = primBlue, shape = CircleShape)
                    .weight(1f)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "send",
                        tint = MaterialTheme.colorScheme.background
                    )
                } // IconButton
            } // if
        } // Row
    } // Column
}
