package com.example.quicktalkcompose.find_contacts_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.home_screen.HomeViewModel
import com.example.quicktalkcompose.ui.theme.grayText
import com.example.quicktalkcompose.ui.theme.primBlue
import kotlinx.coroutines.delay


@Composable
fun FindContactsScreen(navController: NavHostController, homeViewModel: HomeViewModel){
    var isLoading by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    val filteredUsers by homeViewModel.filteredUserList.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()

    val searchFieldWidth by animateDpAsState(
        targetValue = if (isSearching) 330.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        )
    )

    if (isLoading) {
        LaunchedEffect(Unit) {
            homeViewModel.findContacts()
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                if (isSearching) isSearching = false
                else navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "backButton",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            } // IconButton

                AnimatedVisibility(
                    visible = isSearching,
                    enter = slideInHorizontally(
                        initialOffsetX = {fullWidth -> fullWidth },
                        animationSpec = tween(400)) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = {fullWidth -> fullWidth },
                        animationSpec = tween(400)) + fadeOut()
                    )
                    {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {homeViewModel.updateSearchQuery(it)},
                        placeholder = { Text("Ex +12229875544", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 1,
                        modifier = Modifier
                            .width(searchFieldWidth))
                }

            if (!isSearching){
                Spacer(Modifier.width(20.dp))

                Text(
                    text = "Find Contacts",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(13.dp)
                )

                Spacer(Modifier.weight(1f))

                IconButton(onClick = {isSearching = true}) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } // Row

        Spacer(Modifier.height(20.dp))

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

        else {

            LazyColumn(
                Modifier
                    .fillMaxWidth()
            ) {
                itemsIndexed(filteredUsers) {index, user ->

                    var isVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(index * 100L)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = {-300 },
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing)) + fadeIn()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                            modifier = Modifier
                                .clickable { navController.navigate("${AppScreens.Messaging.route}/${user.userId}") }
                                .padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = rememberAsyncImagePainter(user.profileImage),
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

                                Column(modifier = Modifier.weight(3f)) {
                                    Text(
                                        text = user.name,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = user.phoneNumber,
                                        color = grayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                } // Column
                            } // Row
                        } // Card
                    } // AnimatedVisibility
                } // Items
            } // LazyColumn
        }
    } // Column
}