package com.example.quicktalkcompose.remote_data_Source

import android.util.Log
import com.example.quicktalkcompose.domain.models.ChatState
import com.example.quicktalkcompose.domain.models.Message
import com.example.quicktalkcompose.domain.models.MessageEvent
import com.example.quicktalkcompose.domain.models.Story
import com.example.quicktalkcompose.domain.models.StoryItem
import com.example.quicktalkcompose.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseDataSource {

    fun signup(uid: String, number: String, onResult: (Boolean) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        val userData = mapOf(
            "name" to "user",
            "phoneNumber" to number,
            "profileImage" to "",
            "description" to "i'm new here",
            "isOnline" to true,
            "createdAt" to System.currentTimeMillis()
        )

        userRef.setValue(userData).addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun saveProfileImageUrlToFirebase(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId ?: return)
        ref.child("profileImage").setValue(imageUrl)
    }

    fun getCurrentInfo(): User{
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val currentRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid!!)
        var senderName = "eww"
        var senderImage = "w"
        currentRef.get().addOnSuccessListener { snapShot ->
            senderName = snapShot.child("name").getValue(String::class.java) ?: ""
            senderImage = snapShot.child("image").getValue(String::class.java) ?: ""
        }
        return User(currentUid, profileImage = senderImage, name = senderName)
    }

    fun getUsersChat(): Flow<List<User>> = callbackFlow {
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val chatRef = FirebaseDatabase.getInstance().getReference("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<User>()
                val jobs = mutableListOf<Job>()
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key ?: continue

                    if (chatId.contains(currentUid)) {
                        val otherUid = chatId.split("_").first { it != currentUid }
                        val messageIsMuted = chatSnapshot.child("participants").child(otherUid).child("messageMuted").getValue(Boolean::class.java) ?: false
                        var lastMessageText = chatSnapshot.child("lastMessage").child("messageText").getValue(String::class.java) ?: ""
                        val lastMessageTime = chatSnapshot.child("lastMessage").child("messageTime").getValue(Long::class.java) ?: 0
                        val lastMessageImage = chatSnapshot.child("lastMessage").child("image").getValue(String::class.java) ?: ""
                        if (lastMessageImage.isNotEmpty() && lastMessageText.isEmpty()) lastMessageText = "Photo"
                        val unreadCount = chatSnapshot.child("unreadMessages").child(currentUid)
                            .child("unreadCount").getValue(Int::class.java) ?: 0

                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(otherUid)
                        val job = launch {
                            val userSnapshot = userRef.get().await()

                        val name = userSnapshot.child("name").getValue(String::class.java) ?: ""
                        val description = userSnapshot.child("description").getValue(String::class.java) ?: ""
                        val phoneNumber = userSnapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                        val profileImage = userSnapshot.child("profileImage").getValue(String::class.java) ?: ""
                        val isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false

                        val user = User(
                            userId = otherUid,
                            name = name,
                            description = description,
                            phoneNumber = phoneNumber,
                            profileImage = profileImage,
                            lastMessage = lastMessageText,
                            lastMessageTime = lastMessageTime,
                            isMuted = messageIsMuted,
                            isOnline = isOnline,
                            unreadCount = unreadCount
                        )
                        val index = chatList.indexOfFirst { it.userId == user.userId }
                        if (index == -1) chatList.add(user)
                        else chatList[index] = user

                    }
                        jobs.add(job)
                    }
                    launch {
                        jobs.joinAll()
                        trySend(chatList.toList())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        chatRef.addValueEventListener(listener)
        awaitClose { chatRef.removeEventListener(listener) }
    }

    fun saveStoryImageToFirebase(imageUrl: String, text: String = "") {
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val storyId = System.currentTimeMillis().toString()
        val ref = FirebaseDatabase.getInstance().getReference("stories").child(currentUid).child(storyId)
        val storyData = mapOf(
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis(),
            "text" to text
        )
        ref.setValue(storyData)
    }

    fun getUserStories(userId: String): Flow<Story> = callbackFlow {
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val seenRef = FirebaseDatabase.getInstance().getReference("storiesSeen").child(currentUid).child(userId)
        val storyRef = FirebaseDatabase.getInstance().getReference("stories").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(storySnap: DataSnapshot) {
                val storyItems = mutableListOf<StoryItem>()
                val now = System.currentTimeMillis()
                seenRef.get().addOnSuccessListener { seenSnap ->
                    for (child in storySnap.children) {
                        val storyId = child.key ?: continue
                        val url = child.child("imageUrl").getValue(String::class.java) ?: continue
                        val text = child.child("text").getValue(String::class.java) ?: continue
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: continue
                        if (now - timestamp < 24 * 60 * 60 * 1000) {
                            val seen = seenSnap.child(storyId).exists()
                            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            val time = formatter.format(timestamp)
                            storyItems.add(StoryItem(storyId, url, time, seen, text))
                        } else {
                            child.ref.removeValue()
                            seenRef.child(storyId).removeValue()
                        }
                    }
                    val isStoryFullySeen = storyItems.all { it.isSeen }
                    if (storyItems.isNotEmpty()) {
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                        userRef.get().addOnSuccessListener { user ->
                            val name = user.child("name").getValue(String::class.java) ?: ""
                            val profileImage = user.child("profileImage").getValue(String::class.java) ?: ""
                            val story = Story(
                                userId = userId,
                                userProfileImage = profileImage,
                                userName = name,
                                stories = storyItems,
                                isSeen = isStoryFullySeen
                            )
                            trySend(story)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        storyRef.addValueEventListener(listener)
        awaitClose { storyRef.removeEventListener(listener) }
      }

    fun getAllUsersStories(): Flow<List<Story>> = callbackFlow {
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val contactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(currentUid)

        contactsRef.get().addOnSuccessListener { snapshot ->
            val contacts = snapshot.children.mapNotNull { it.key }
            val flows = contacts.map { getUserStories(it) }
            launch {
                combine(flows){ array ->
                    array.toList()
                }.collect { list ->
                    trySend(list)
                }
            }
        }
        awaitClose {  }
    }

    fun setStoriesSeen(userId: String, storyId : String) {
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val seenRef = FirebaseDatabase.getInstance().getReference("storiesSeen").child(currentUid).child(userId).child(storyId)
        seenRef.setValue(true)
    }

    fun findContacts(onResult: (List<User>) -> Unit) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val contactRef = FirebaseDatabase.getInstance().getReference("users")
        val contactList = mutableListOf<User>()

        contactRef.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val userId = child.key
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val phoneNumber = child.child("phoneNumber").getValue(String::class.java) ?: ""
                    val profileImage =
                        child.child("profileImage").getValue(String::class.java) ?: ""

                    val user = User(
                        userId = userId!!,
                        name = name,
                        description = description,
                        phoneNumber = phoneNumber,
                        profileImage = profileImage
                    )
                    if (user.userId != currentUid) {
                        contactList.add(user)
                        onResult(contactList)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "onCancelled: ${error.message}")
            }
        })
    }

    fun generateChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
    }

    fun sendMessage(
        receiverUid: String,
        messageText: String,
        repliedTo: Message? = null,
        imageUrl: String? = null,
        status: String = "Sent",
        storyReply : Boolean = false,
        storyText: String = "",
        storyImage: String = ""
    ) {
        val senderUid = FirebaseAuth.getInstance().currentUser!!.uid
        val chatId = generateChatId(senderUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)
        val contactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(senderUid)

        val messageData = mapOf(
            "messageText" to messageText,
            "senderId" to senderUid,
            "messageTime" to ServerValue.TIMESTAMP,
            "messageDate" to ServerValue.TIMESTAMP,
            "repliedTo" to repliedTo,
            "image" to imageUrl,
            "status" to status,
            "storyReply" to storyReply,
            "storyText" to storyText,
            "storyImage" to storyImage
        )
        messageRef.child("participants").child(senderUid).child("isInChat").get()
            .addOnSuccessListener {
                if (!it.exists()) messageRef.child("participants").child(senderUid)
                    .child("isInChat").setValue(true)
            }
        messageRef.child("participants").child(receiverUid).child("isInChat").get()
            .addOnSuccessListener {
                if (!it.exists()) messageRef.child("participants").child(receiverUid)
                    .child("isInChat").setValue(false)
            }

        contactsRef.child(receiverUid).setValue(true)

        messageRef.child("lastMessage").setValue(messageData)

        if (status == "Sent") {
            val unreadRef =
                messageRef.child("unreadMessages").child(receiverUid).child("unreadCount")
            unreadRef.get().addOnSuccessListener {
                val currentCount = it.getValue(Int::class.java) ?: 0
                unreadRef.setValue(currentCount + 1)
            }
        }

        val messageId = messageRef.child("chatContent").push().key!!
        messageRef.child("chatContent").child(messageId).setValue(messageData)
    }

    fun formatTime(timeStamp : Long?): String{
        if (timeStamp == null) return ""
        val date = Date(timeStamp)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format((date))
    }

    fun formatDate(timeStamp : Long?): String{
        if (timeStamp == null) return ""
        val date = Date(timeStamp)
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return format.format((date))
    }

    fun listenForMessages(currentUid: String, receiverUid: String): Flow<MessageEvent> =
        callbackFlow {
            val chatId = generateChatId(currentUid, receiverUid)
            val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId).child("chatContent")

            val listener = object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messageId = snapshot.key!!
                    val messageText = snapshot.child("messageText").getValue(String::class.java) ?: ""
                    val messageTime = snapshot.child("messageTime").getValue(Long::class.java) ?: 0
                    val messageDate = snapshot.child("messageDate").getValue(Long::class.java) ?: 0
                    val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
                    val repliedTo = snapshot.child("repliedTo").getValue(Message::class.java)
                    repliedTo?.messageTime = snapshot.child("repliedTo").child("messageTime").getValue(Long::class.java) ?: 0
                    repliedTo?.messageDate = snapshot.child("repliedTo").child("messageDate").getValue(Long::class.java) ?: 0
                    val image = snapshot.child("image").getValue(String::class.java) ?: ""
                    val reactionCount = snapshot.child("reactionCount").getValue(Int::class.java) ?: 0
                    val isEdited = snapshot.child("isEdited").getValue(Boolean::class.java) ?: false
                    val status = snapshot.child("status").getValue(String::class.java) ?: ""
                    val storyReply = snapshot.child("storyReply").getValue(Boolean::class.java) ?: false
                    val storyText = snapshot.child("storyText").getValue(String::class.java) ?: ""
                    val storyImage = snapshot.child("storyImage").getValue(String::class.java) ?: ""
                   val message = Message(
                       messageId,
                       messageText,
                       messageTime,
                       messageDate,
                       senderId,
                       repliedTo,
                       image,
                       reactionCount,
                       isEdited,
                       status,
                       storyReply,
                       storyText,
                       storyImage
                   )
                    Log.d("TAG", "onChildAdded: sender id is $senderId")
                    trySend(MessageEvent.Added(message))
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val messageId = snapshot.key!!
                    val messageText = snapshot.child("messageText").getValue(String::class.java) ?: ""
                    val messageTime = snapshot.child("messageTime").getValue(Long::class.java) ?: 0
                    val messageDate = snapshot.child("messageDate").getValue(Long::class.java) ?: 0
                    val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
                    val repliedTo = snapshot.child("repliedTo").getValue(Message::class.java)
                    repliedTo?.messageTime = snapshot.child("repliedTo").child("messageTime").getValue(Long::class.java) ?: 0
                    repliedTo?.messageDate = snapshot.child("repliedTo").child("messageDate").getValue(Long::class.java) ?: 0
                    val image = snapshot.child("image").getValue(String::class.java) ?: ""
                    val reactionCount = snapshot.child("reactionCount").getValue(Int::class.java) ?: 0
                    val isEdited = snapshot.child("isEdited").getValue(Boolean::class.java) ?: false
                    val status = snapshot.child("status").getValue(String::class.java) ?: ""
                    val storyReply = snapshot.child("storyReply").getValue(Boolean::class.java) ?: false
                    val storyText = snapshot.child("storyText").getValue(String::class.java) ?: ""
                    val storyImage = snapshot.child("storyImage").getValue(String::class.java) ?: ""
                    val message = Message(
                        messageId,
                        messageText,
                        messageTime,
                        messageDate,
                        senderId,
                        repliedTo,
                        image,
                        reactionCount,
                        isEdited,
                        status,
                        storyReply,
                        storyText,
                        storyImage
                    )
                    trySend(MessageEvent.Changed(message))
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val messageId = snapshot.key!!
                    val messageText = snapshot.child("messageText").getValue(String::class.java) ?: ""
                    val messageTime = snapshot.child("messageTime").getValue(Long::class.java) ?: 0
                    val messageDate = snapshot.child("messageDate").getValue(Long::class.java) ?: 0
                    val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
                    val repliedTo = snapshot.child("repliedTo").getValue(Message::class.java)
                    repliedTo?.messageTime = snapshot.child("repliedTo").child("messageTime").getValue(Long::class.java) ?: 0
                    repliedTo?.messageDate = snapshot.child("repliedTo").child("messageDate").getValue(Long::class.java) ?: 0
                    val image = snapshot.child("image").getValue(String::class.java) ?: ""
                    val reactionCount = snapshot.child("reactionCount").getValue(Int::class.java) ?: 0
                    val isEdited = snapshot.child("isEdited").getValue(Boolean::class.java) ?: false
                    val status = snapshot.child("status").getValue(String::class.java) ?: ""
                    val storyReply = snapshot.child("storyReply").getValue(Boolean::class.java) ?: false
                    val storyText = snapshot.child("storyText").getValue(String::class.java) ?: ""
                    val storyImage = snapshot.child("storyImage").getValue(String::class.java) ?: ""
                    val message = Message(
                        messageId,
                        messageText,
                        messageTime,
                        messageDate,
                        senderId,
                        repliedTo,
                        image,
                        reactionCount,
                        isEdited,
                        status,
                        storyReply,
                        storyText,
                        storyImage
                    )
                    trySend(MessageEvent.Deleted(message))
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TAG", "onCancelled: ${error.message}")
                }
            }
            messageRef.addChildEventListener(listener)
            awaitClose { messageRef.removeEventListener(listener) }
        }

    fun editMessage(receiverUid: String, messageText: String, messageId: String) {
        val senderUid = FirebaseAuth.getInstance().currentUser!!.uid
        val chatId = generateChatId(senderUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId).child("chatContent")
        messageRef.child(messageId).child("messageText").setValue(messageText)
        messageRef.child(messageId).child("isEdited").setValue(true)
    }

    fun deleteMessage(currentUid: String, receiverUid: String, messageId: String){
        val chatId = generateChatId(currentUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)
        val deleteMessageRef = messageRef.child("chatContent").child(messageId)
        deleteMessageRef.removeValue()
    }

    fun setCurrentIsInChat(currentUid: String, receiverUid: String, isDisposed: Boolean){
        val chatId = generateChatId(currentUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)
        if (!isDisposed) {
            messageRef.child("participants").child(currentUid).child("isInChat").setValue(true)
            messageRef.child("participants").child(currentUid).child("isInChat").onDisconnect().setValue(false)
        }
        else messageRef.child("participants").child(currentUid).child("isInChat").setValue(false)
    }

    fun setCurrentIsTyping(messageText: String, currentUid: String, receiverUid: String) {
        val chatId = generateChatId(currentUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)
        if (messageText.isNotEmpty()) {
            messageRef.child("participants").child(currentUid).child("isTyping").setValue(true)
        } else messageRef.child("participants").child(currentUid).child("isTyping").setValue(false)
    }

    fun observeChatState(currentUid: String, receiverUid: String): Flow<ChatState> = callbackFlow {
        val chatId = generateChatId(currentUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)
        var state = ChatState()

        val receiverIsTypingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state =
                    state.copy(receiverIsTyping = snapshot.getValue(Boolean::class.java) ?: false)
                trySend(state)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "onCancelled: ${error.message}")
            }
        }

        val receiverIsInChatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state =
                    state.copy(receiverIsInChat = snapshot.getValue(Boolean::class.java) ?: false)
                trySend(state)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "onCancelled: ${error.message}")
            }
        }

        val receiverIsMutedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state =
                    state.copy(receiverIsMuted = snapshot.getValue(Boolean::class.java) ?: false)
                trySend(state)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "onCancelled: ${error.message}")
            }
        }


        val currentIsMutedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state = state.copy(currentIsMuted = snapshot.getValue(Boolean::class.java) ?: false)
                trySend(state)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "onCancelled: ${error.message}")
            }
        }
        messageRef.child("participants").child(receiverUid).child("isTyping")
            .addValueEventListener(receiverIsTypingListener)
        messageRef.child("participants").child(receiverUid).child("isInChat")
            .addValueEventListener(receiverIsInChatListener)
        messageRef.child("participants").child(receiverUid).child("messageMuted")
            .addValueEventListener(receiverIsMutedListener)
        messageRef.child("participants").child(currentUid).child("messageMuted")
            .addValueEventListener(currentIsMutedListener)

        awaitClose {
            messageRef.child("participants").child(receiverUid).child("isTyping")
                .removeEventListener(receiverIsTypingListener)
            messageRef.child("participants").child(receiverUid).child("isInChat")
                .removeEventListener(receiverIsInChatListener)
            messageRef.child("participants").child(receiverUid).child("messageMuted")
                .removeEventListener(receiverIsMutedListener)
            messageRef.child("participants").child(currentUid).child("messageMuted")
                .removeEventListener(currentIsMutedListener)
        }
    }

    fun setMessagesStatusToRead(currentUid: String, receiverUid: String) {
        val chatId = generateChatId(currentUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)
        messageRef.child("chatContent").get().addOnSuccessListener { snapshot ->
            for (msg in snapshot.children) {
                val status = msg.child("status").getValue(String::class.java) ?: ""
                val senderId = msg.child("senderId").getValue(String::class.java) ?: ""
                if (status != "Seen" && senderId == receiverUid) {
                    msg.ref.child("status").setValue("Seen")
                }
                messageRef.child("unreadMessages").child(currentUid).child("unreadCount")
                    .setValue(0)
            }
        }
    }

    fun setReactionToMessage(currentUid: String, receiverUid: String, messageId: String, sendNotification : () -> Unit) {
        val chatId = generateChatId(currentUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)

        messageRef.child("chatContent").child(messageId).child("reactions").child(currentUid).get().addOnSuccessListener {
                if (!it.exists()) {
                    messageRef.child("chatContent").child(messageId).child("reactions").child(currentUid).setValue(true)
                    sendNotification()
                }
                else messageRef.child("chatContent").child(messageId).child("reactions").child(currentUid).removeValue()
            }
    }

    fun muteTheUser(currentUid: String, receiverUid: String, isMuted : Boolean){
        val chatId = generateChatId(currentUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId)

        if (isMuted) messageRef.child("participants").child(receiverUid).child("messageMuted").setValue(false)
        else messageRef.child("participants").child(receiverUid).child("messageMuted").setValue(true)
    }
}