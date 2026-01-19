package com.example.quicktalkcompose.start_splash_screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quicktalkcompose.R
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.ui.theme.primBlue

@Composable
fun StartNowScreen(navController: NavHostController){

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = "Welcome to QuickTalk",
            color = primBlue,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(100.dp))

        Image(
            painter = painterResource(id = R.drawable.chattingimage),
            contentDescription = "logo",
            modifier = Modifier.size(300.dp)
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                navController.navigate(AppScreens.SignUp.route)
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
                Text(text = "Start", color = Color.White, fontSize = 18.sp)
            } // Box
        } // Button

    }
}


@Composable
@Preview(showBackground = true)
fun StartNowScreenPreview(){
    val nav = rememberNavController()
    StartNowScreen(nav)
}