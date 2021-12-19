# cherie-android
This repository has codebase for Android mobile application.



## Project characteristics and tech-stack

This project apply simple MVVM design pattern.

 - Tech-stack
   - [Kotlin](https://kotlinlang.org/) + [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - perform background operations
   - [Retrofit](https://square.github.io/retrofit/) - networking
   - [Jetpack](https://developer.android.com/jetpack)
        - [Navigation](https://developer.android.com/guide/navigation) - in-app navigation
        - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - notify views about data change
        - [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) - perform an action when lifecycle state changes
        - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - store and manage UI-related data in a lifecycle conscious way
    - [Glide](https://bumptech.github.io/glide/) - image loading library
    - [Proxyman](https://proxyman.io/) - network proxy debugging
- Modern Architecture
    -  [Android Architecture components](https://developer.android.com/topic/architecture) (ViewModel, LiveData, Navigation)
    - [Android KTX](https://developer.android.com/kotlin/ktx) - Jetpack Kotlin extensions
- UI
    - [Material Design](https://material.io/design)
    - [Material Theming Guide](https://material.io/develop/android/theming/theming-overview)
- Gradle
    - Plugins ([SafeArgs](https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args))


## Getting started

### Command-line + Android Studio
1. Run 
`git clone git@github.com:AR7-lab/cherie-android.git` command to clone project.

2. Open `Android Studio` and select `File | Open...` from the menu. Select cloned directory and press `Open` button