# AquaGlow - Android Wellness App

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)

> **Personal wellness Android app with daily habit tracking, mood journal, and hydration reminders. Built with Kotlin and Android Studio for SLIIT Mobile Application Development (MAD) Lab Exam 3 - IT2010.**

## 📱 About AquaGlow

AquaGlow is a comprehensive wellness application designed to help users maintain a healthy lifestyle through habit tracking, mood monitoring, and hydration management. The app provides an intuitive interface for users to track their daily habits, log their emotional state, and receive timely hydration reminders.

## ✨ Features

### 🎯 **Habit Tracking**
- Create and manage daily habits
- Track habit completion with visual indicators
- Set custom habit categories
- View habit statistics and progress

### 😊 **Mood Journal**
- Log daily mood entries with emoji selection
- Track mood patterns over time
- View mood history and trends
- Emotional wellness insights

### 💧 **Hydration Reminders**
- Customizable water intake goals
- Smart reminder notifications
- Track daily water consumption
- Visual progress indicators

### 📊 **Statistics & Analytics**
- Comprehensive habit completion rates
- Mood trend analysis
- Hydration tracking statistics
- Weekly and monthly reports

### ⚙️ **Settings & Customization**
- Personal profile management
- Notification preferences
- Theme customization
- Data export options

## 🛠️ Technical Details

### **Development Environment**
- **Platform:** Android
- **Language:** Kotlin
- **IDE:** Android Studio
- **Target SDK:** Latest Android API
- **Minimum SDK:** API 21 (Android 5.0)

### **Architecture & Design Patterns**
- **MVVM Architecture** for clean code separation
- **Repository Pattern** for data management
- **WorkManager** for background tasks
- **SharedPreferences** for local data storage
- **Material Design** principles

### **Key Components**
- **Activities:** MainActivity, OnboardingActivity, WelcomeActivity
- **Fragments:** HabitsFragment, MoodFragment, StatisticsFragment, SettingsFragment
- **Workers:** HydrationReminderWorker, HabitResetWorker
- **Utilities:** DataCache, ImageUtils, PerformanceMonitor

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/aquaglow/
│   │   ├── Activities/          # Main app activities
│   │   ├── Fragments/           # UI fragments
│   │   ├── Workers/             # Background workers
│   │   └── Utils/               # Utility classes
│   ├── res/
│   │   ├── layout/              # XML layouts
│   │   ├── drawable/            # Icons and graphics
│   │   ├── values/              # Strings, colors, themes
│   │   └── navigation/          # Navigation graphs
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## 🚀 Getting Started

### **Prerequisites**
- Android Studio (Latest version)
- Android SDK (API 21+)
- Kotlin support enabled

### **Installation**
1. Clone the repository:
   ```bash
   git clone https://github.com/subasthican/AquaGlow-Android-MAD-Exam3.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build and run the app on an emulator or physical device

### **Building the APK**
```bash
./gradlew assembleDebug
```

## 📱 Screenshots

*Screenshots will be added to showcase the app's UI and features*

## 🎓 Academic Information

**Course:** Mobile Application Development (MAD) - IT2010  
**Institution:** Sri Lanka Institute of Information Technology (SLIIT)  
**Exam:** Lab Exam 3  
**Student:** Subasthican Manoharan  
**Submission Date:** January 2025  

## 🔧 Dependencies

- **AndroidX Libraries** - Modern Android development
- **Material Design Components** - UI components
- **WorkManager** - Background task management
- **Navigation Component** - Fragment navigation
- **ViewBinding** - Type-safe view binding

## 📝 Features Implementation

### **Core Features Completed:**
- ✅ User onboarding flow
- ✅ Habit creation and tracking
- ✅ Mood logging system
- ✅ Hydration reminder system
- ✅ Statistics and analytics
- ✅ Settings and preferences
- ✅ Data persistence
- ✅ Background notifications

### **UI/UX Features:**
- ✅ Material Design implementation
- ✅ Responsive layouts
- ✅ Smooth animations
- ✅ Dark theme support
- ✅ Intuitive navigation

## 🤝 Contributing

This is an academic project for SLIIT MAD Lab Exam 3. For any questions or feedback, please contact the developer.

## 📄 License

This project is created for educational purposes as part of SLIIT Mobile Application Development course requirements.

## 👨‍💻 Developer

**Subasthican Manoharan**  
- GitHub: [@subasthican](https://github.com/subasthican)
- LinkedIn: [in/manoharansubasthican](https://linkedin.com/in/manoharansubasthican)
- Institution: SLIIT - Faculty of Computing

---

*Built with ❤️ for SLIIT Mobile Application Development Lab Exam 3*
