# NutriSnap 🥗
### AI-Powered Nutrition Tracking for Android

A premium native Android calorie tracking app built with Jetpack Compose and Google Gemini AI — competing with top-tier health apps like Cal AI and MyFitnessPal.

---

## ✨ Features

### 🤖 AI Integration
- **Photo Food Recognition** — Snap a photo, get instant macro breakdown via Gemini 1.5 Flash
- **Multi-Food Detection** — Detect multiple dishes in a single photo
- **AI Nutrition Insights** — Dynamic, personalized cards ("You need 32g more protein today")
- **AI BMI Analysis** — Personalized health recommendations based on your body metrics
- **Food Name Editing** — Correct AI suggestions before logging

### 📔 Food Diary
- Daily food diary with running calorie total
- Meal grouping (Breakfast / Lunch / Dinner / Snacks)
- Portion size adjuster (0.25x → 3x) with real-time macro recalculation
- Swipe to delete entries
- Edit any logged entry
- Search & re-add past foods (Quick Add)

### 📊 Data Visualization
- Animated macro progress rings (Protein / Carbs / Fat)
- Gradient calorie progress bar
- Animated macro pie chart with percentage breakdown
- Interactive weekly bar chart (tap any bar for day details)
- Goal progress bars with smooth animations

### 👤 Profile & Goals
- 4-step onboarding (Name → Body Metrics → Activity → Goal)
- TDEE-based calorie calculation (Mifflin-St Jeor equation)
- BMI gauge with animated needle
- 2×2 macro goals grid with bottom sheet editing
- Streak tracking with animated flame badge

### 🎨 UI/UX
- Deep dark mode with neon green accents
- Glassmorphism cards with frosted glass effect
- Shimmer loading states during AI analysis
- Success animation with particle burst + haptic feedback
- Splash screen with spring animations
- Elevated center FAB in navigation bar with pulsing glow
- Smooth screen transitions

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| AI | Google Gemini 1.5 Flash |
| Database | Room |
| Architecture | MVVM + StateFlow |
| DI | Hilt |
| Image Loading | Coil |
| HTTP | OkHttp |
| Navigation | Compose Navigation |
| Min SDK | API 26 (Android 8.0) |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android device / emulator (API 26+)
- Google Gemini API key (free at [aistudio.google.com](https://aistudio.google.com))

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/CalTracker.git
cd CalTracker
```

2. **Add your Gemini API key**

Open `local.properties` and add:
```
GEMINI_API_KEY=your_api_key_here
```

3. **Build and run**
```bash
./gradlew assembleDebug
```
Or press ▶ Run in Android Studio.

---

## 📱 Screenshots

> Home Screen · Log Food · Diary · Progress · Profile

---

## 📁 Project Structure

```
app/src/main/java/com/example/caltracker/
├── data/
│   ├── Models.kt          # FoodEntry, UserProfile, DailyGoal
│   ├── FoodDao.kt         # Room DAO
│   └── AppDatabase.kt     # Room Database
├── network/
│   └── ClaudeApiService.kt  # Gemini API integration
├── di/
│   └── AppModule.kt       # Hilt dependency injection
├── repository/
│   └── FoodRepository.kt  # Data layer
├── viewmodel/
│   └── MainViewModel.kt   # UI state management
└── uii/
    ├── HomeScreen.kt       # AI dashboard
    ├── LogFoodScreen.kt    # Camera + AI analysis
    ├── DiaryScreen.kt      # Food diary
    ├── ProgressScreen.kt   # Charts + analytics
    ├── ProfileScreen.kt    # BMI + goals
    ├── RegisterScreen.kt   # Onboarding
    ├── SplashScreen.kt     # Launch animation
    ├── Components.kt       # Shared UI components
    ├── InsightsEngine.kt   # AI insight generation
    └── Navigation.kt       # App navigation
```

---

## 🔑 API Key Security

- API key stored in `local.properties` (gitignored)
- Never committed to version control
- Baked into `BuildConfig` at compile time

---

## 📄 License

MIT License — feel free to use and modify.

---

Built with ❤️ using Jetpack Compose + Gemini AI
