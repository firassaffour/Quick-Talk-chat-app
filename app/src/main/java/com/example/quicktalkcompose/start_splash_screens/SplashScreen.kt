package com.example.quicktalkcompose.start_splash_screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.domain.models.BottomNavItem
import com.example.quicktalkcompose.signup_screen.AuthState
import com.example.quicktalkcompose.signup_screen.SignUpViewModel
import com.example.quicktalkcompose.isDark
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavHostController, authViewModel: SignUpViewModel){
    val authState = authViewModel.authState.observeAsState()
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAni = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(3000)
    )
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(4000)
        navController.popBackStack()
        val next = if (authState.value == AuthState.Unauthenticated) AppScreens.StartNow.route
        else BottomNavItem.Home.route

        navController.navigate(next)
    } // LaunchedEffect
    Splash(alphaAni.value)
} // AnimatedSplashScreen

@Composable
fun Splash(alpha : Float){
    Column (modifier = Modifier
        .background(color = MaterialTheme.colorScheme.background)
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally){

        Spacer(Modifier.weight(1f))


        Icon( modifier = Modifier.size(200.dp)
            .weight(6f)
            .alpha(alpha)
            .graphicsLayer(alpha = 0.99f),
            painter = if (isDark) painterResource(id = R.drawable.quicktallklightlogo)
            else painterResource(id = R.drawable.quicktallkdarklogo),
            contentDescription = "logo icon")

        Text(modifier = Modifier
            .weight(1f)
            .alpha(alpha),
            text = "by Firas",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp
        )
    } // Box
} // Splash