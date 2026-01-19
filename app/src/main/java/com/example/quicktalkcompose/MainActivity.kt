package com.example.quicktalkcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quicktalkcompose.home_screen.HomeViewModel
import com.example.quicktalkcompose.signup_screen.SignUpViewModel
import com.example.quicktalkcompose.preference_manager.ThemePreferenceManager
import com.example.quicktalkcompose.call_screen.CallsScreen
import com.example.quicktalkcompose.domain.models.AppScreens
import com.example.quicktalkcompose.domain.models.BottomNavItem
import com.example.quicktalkcompose.edit_name_description.EditDescriptionScreen
import com.example.quicktalkcompose.edit_name_description.EditNameScreen
import com.example.quicktalkcompose.find_contacts_screen.FindContactsScreen
import com.example.quicktalkcompose.home_screen.EditStoryScreen
import com.example.quicktalkcompose.home_screen.HomeRepository
import com.example.quicktalkcompose.signup_screen.FirstSignUpScreen
import com.example.quicktalkcompose.home_screen.HomeScreen
import com.example.quicktalkcompose.home_screen.HomeViewModelFactory
import com.example.quicktalkcompose.messaging_screen.MessagingScreen
import com.example.quicktalkcompose.settings_screen.SettingsScreen
import com.example.quicktalkcompose.signup_screen.SignUpScreen
import com.example.quicktalkcompose.start_splash_screens.SplashScreen
import com.example.quicktalkcompose.start_splash_screens.StartNowScreen
import com.example.quicktalkcompose.home_screen.StoriesViewer
import com.example.quicktalkcompose.user_profile_screen.UserProfileScreen
import com.example.quicktalkcompose.messaging_screen.MessagingRepository
import com.example.quicktalkcompose.messaging_screen.MessagingViewModel
import com.example.quicktalkcompose.messaging_screen.MessagingViewModelFactory
import com.example.quicktalkcompose.remote_data_Source.CloudinaryDataSource
import com.example.quicktalkcompose.remote_data_Source.FirebaseDataSource
import com.example.quicktalkcompose.remote_data_Source.OneSignalDataSource
import com.example.quicktalkcompose.signup_screen.SignUpRepository
import com.example.quicktalkcompose.signup_screen.SignUpViewModelFactory
import com.example.quicktalkcompose.ui.theme.QuickTalkComposeTheme
import com.example.quicktalkcompose.ui.theme.primBlue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            ThemePreferenceManager.getThemePreference(applicationContext)
                .collect {savedValue ->
                    isDark = savedValue
                }
        }
        setContent {
            QuickTalkComposeTheme(darkTheme = isDark) {
                // setting status bar and navigation bar color
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(if (isDark) Color(0xFF121212) else Color.White, darkIcons = !isDark)
                    systemUiController.setNavigationBarColor(if (isDark) Color(0xFF121212) else Color.White, darkIcons = !isDark)

                }

                val navController = rememberNavController()

                // View Models
                val firebaseDataSource = remember { FirebaseDataSource() }
                val cloudinaryDataSource = remember { CloudinaryDataSource() }
                val oneSignalDataSource = remember { OneSignalDataSource() }

                // Home View Model
                val homeRepository = remember { HomeRepository(firebaseDataSource, cloudinaryDataSource, oneSignalDataSource) }
                val homeFactory = HomeViewModelFactory(homeRepository, application)
                val homeViewModel = ViewModelProvider(this, homeFactory)[HomeViewModel::class.java]

                // Sign Up View Model
                val signUpRepository = remember { SignUpRepository(firebaseDataSource, cloudinaryDataSource) }
                val signUpFactory = SignUpViewModelFactory(signUpRepository, application)
                val signUpViewModel = ViewModelProvider(this, signUpFactory)[SignUpViewModel::class.java]

                // Messaging View Model
                val messagingRepository = remember { MessagingRepository(firebaseDataSource, cloudinaryDataSource, oneSignalDataSource) }
                val messagingFactory = MessagingViewModelFactory(messagingRepository, application)
                val messagingViewModel = ViewModelProvider(this, messagingFactory)[MessagingViewModel::class.java]

                MainInsideApp(navController, homeViewModel, signUpViewModel, messagingViewModel)

                LaunchedEffect(isDark) {
                    lifecycleScope.launch {
                        ThemePreferenceManager.saveThemePreference(applicationContext, isDark)
                    }
                }
                homeViewModel.initOneSignal(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid)
        userRef.child("isOnline").setValue(true)
        userRef.child("isOnline").onDisconnect().setValue(false)
    }

    override fun onStop() {
        super.onStop()
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid)
        userRef.child("isOnline").setValue(false)
    }
}

var isDark by mutableStateOf(false)

@Composable
fun BottomNavigationBar(navController: NavController){
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        BottomNavItem.items.forEach{ item ->
            NavigationBarItem(icon = { Icon(item.icon, contentDescription = item.title, tint = MaterialTheme.colorScheme.onBackground) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemColors(
                    selectedIndicatorColor = primBlue,
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledIconColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground
                ),
                onClick = {navController.navigate(item.route) {
                    popUpTo(BottomNavItem.Home.route) {saveState = true}
                    launchSingleTop = true
                    restoreState = true
                } } /* onClick */ )
        }
    } // NavigationBar
} // BottomNavigationBar

@Composable
fun MainInsideApp(navController : NavHostController, homeViewModel: HomeViewModel, signUpViewModel: SignUpViewModel, messagingViewModel: MessagingViewModel){
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    val usersList by homeViewModel.usersList.collectAsState()
    val storiesList by homeViewModel.storiesList.collectAsState()
    val filteredUsers by homeViewModel.filteredUserList.collectAsState()
    val myStory by homeViewModel.myStory.collectAsState()

    Scaffold(bottomBar = { if (currentRoute in BottomNavItem.items.map { it.route }) {BottomNavigationBar(navController) }}) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.Splash.route,
            modifier = Modifier.padding(innerPadding)){
            composable(AppScreens.StartNow.route){ StartNowScreen(navController) }
            composable(AppScreens.Splash.route){ SplashScreen(navController, signUpViewModel) }
            composable(AppScreens.SignUp.route){ SignUpScreen(navController, signUpViewModel) }
            composable(AppScreens.FirstSignUp.route){ FirstSignUpScreen(navController) }
            composable(BottomNavItem.Home.route){ HomeScreen(navController, homeViewModel) }
            composable("${ AppScreens.StoryViewer.route }/{storyId}", arguments = listOf(navArgument("storyId") {type = NavType.StringType})){ backStack ->
                val storyId = backStack.arguments?.getString("storyId")
                if (storyId == currentUid) StoriesViewer(navController, myStory, homeViewModel)
                else {
                    val story = storiesList.firstOrNull { it.userId == storyId }
                    if (story != null) {
                        StoriesViewer(navController, story, homeViewModel)
                    }
                }
            } // Composable
            composable(AppScreens.EditStory.route){ EditStoryScreen(navController, homeViewModel) }
            composable(AppScreens.FindContacts.route){ FindContactsScreen(navController, homeViewModel) }
            composable(BottomNavItem.Calls.route){ CallsScreen() }
            // Settings
            composable(AppScreens.Settings.route){ SettingsScreen(navController, signUpViewModel) }
            composable("${AppScreens.EditName.route}/{name}", arguments = listOf(navArgument("name") {type = NavType.StringType})){ backStack ->
                val name = backStack.arguments?.getString("name")
                EditNameScreen(navController, name!!)
            } // Composable
            composable("${AppScreens.EditDescription.route}/{description}", arguments = listOf(navArgument("description") {type = NavType.StringType})){ backStack ->
                val description = backStack.arguments?.getString("description")
                EditDescriptionScreen(navController, description!!)
            } // Composable

            composable("${AppScreens.Messaging.route}/{userId}", arguments = listOf(navArgument("userId") {type = NavType.StringType})){ backStack ->
                val userId = backStack.arguments?.getString("userId")
                val allUsers = usersList + filteredUsers
                val user = allUsers.firstOrNull {it.userId == userId}
                if (user != null){
                    MessagingScreen(navController, user, messagingViewModel)
                }
            } // Composable
            composable("${AppScreens.UserProfile.route}/{userId}", arguments = listOf(navArgument("userId") {type = NavType.StringType})){ backStack ->
                val userId = backStack.arguments?.getString("userId")
                val user = usersList.firstOrNull {it.userId == userId}
                if (user != null){
                    UserProfileScreen(navController, user)
                }
            } // Composable
        } // NavHost
    } // Scaffold
}