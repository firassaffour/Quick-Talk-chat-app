package com.example.quicktalkcompose.domain.models

sealed class AppScreens(val route : String){
    object Splash : AppScreens("splashScreen")
    object StartNow : AppScreens("startNowScreen")
    object Settings : AppScreens("settingsScreen")
    object Messaging : AppScreens("messagingScreen")
    object FindContacts : AppScreens("findContactsScreen")
    object UserProfile : AppScreens("userProfileScreen")
    object SignUp : AppScreens("signUpScreen")
    object FirstSignUp : AppScreens("firstSignUpScreen")
    object EditName : AppScreens("EditNameScreen")
    object EditDescription : AppScreens("EditDescriptionScreen")
    object StoryViewer : AppScreens("StoryViewer")
    object EditStory : AppScreens("EditStory")
}