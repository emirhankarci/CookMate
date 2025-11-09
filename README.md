# 🍳 CookMate

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)

**CookMate** is a modern Android cooking app that brings couples together in the kitchen through real-time collaborative cooking experiences. Cook together, step by step, from anywhere in the world! 👫🍳

## ✨ Features

### 🔥 Core Features
- **Real-time Collaborative Cooking**: Cook with your partner simultaneously
- **Step-by-Step Guidance**: Interactive cooking instructions with animations
- **Multi-Country Recipes**: Authentic recipes from France, Italy, Turkey, and more
- **Gender-Based Profiles**: Personalized cooking experience for couples
- **Progress Tracking**: Monitor your cooking journey and completed recipes

### 🎨 UI/UX Highlights
- **Modern Material Design 3**: Beautiful, intuitive interface
- **Lottie Animations**: Engaging cooking animations for each step
- **Dynamic Themes**: Country-specific color schemes and backgrounds
- **Responsive Design**: Optimized for different screen sizes
- **Smooth Transitions**: Fluid animations and state management

### 🌍 Multi-Country Experience
- **France**: Classic French cuisine with official flag colors
- **Italy**: Traditional Italian recipes
- **Turkey**: Authentic Turkish dishes
- **More countries coming soon!**

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Backend**: Firebase Realtime Database
- **Authentication**: Firebase Auth
- **Animations**: Lottie Compose
- **Navigation**: Navigation Compose

### Project Structure
```
app/
├── src/main/java/com/emirhankarci/cookmate/
│   ├── data/
│   │   ├── model/          # Data models
│   │   └── repository/     # Data repositories
│   ├── presentation/
│   │   ├── auth/          # Authentication screens
│   │   ├── countries/     # Country selection
│   │   ├── recipes/       # Recipe listing
│   │   ├── cooking/       # Cooking session
│   │   └── components/    # Reusable UI components
│   └── di/                # Dependency injection
└── src/main/res/
    ├── drawable/          # Images and icons
    └── raw/              # Lottie animation files
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or newer
- Android SDK 27 or higher
- Kotlin 1.9.0+
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/emirhankarci/cookmate.git
   cd cookmate
   ```

2. **Firebase Setup**
   - Create a new Firebase project
   - Enable Authentication and Realtime Database
   - Download `google-services.json` and place it in `app/` directory

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## 📱 Screenshots

### Authentication Flow
- **Welcome Screen**: Animated onboarding with Lottie animations
- **User Selection**: Gender-based profile selection for couples

### Cooking Experience
- **Country Selection**: Beautiful country cards with flag animations
- **Recipe Browser**: Grid and list view with filtering options
- **Cooking Session**: Real-time collaborative cooking with step animations

### Key Screens
- **Country List**: Dynamic grid/list toggle with Lottie flag animations
- **Recipe Cards**: Background images with cooking-themed designs
- **Cooking Steps**: Interactive animations based on cooking actions

## 🎬 Animations & Assets

### Lottie Animations
The app features custom Lottie animations for enhanced user experience:

- **Cooking Actions**: `vegetable_cutting.json`, `heat_the_pan.json`, `saute_food.json`
- **Food Items**: `tomato.json`, `add_food.json`
- **Characters**: `man_cooking.json`, `woman_cooking.json`
- **UI Elements**: `cloud_sync.json`, `heart_jumping.json`
- **Country Flags**: `france_flag.json`, `italy_flag.json`, `turkey_flag.json`

### Performance Optimization
- Efficient animation loading and caching
- Optimized file sizes (average 50-200KB per animation)
- Smart animation selection based on cooking steps

## 🔧 Configuration

### Firebase Rules
```javascript
{
  "rules": {
    "countries": {
      ".read": "auth != null",
      ".write": false
    },
    "recipes": {
      ".read": "auth != null",
      ".write": false
    },
    "sessions": {
      "$sessionId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

### Build Configuration
- **Min SDK**: 27 (Android 8.1)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 36
- **Kotlin**: 1.9.0
- **Compose BOM**: 2024.02.00

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Ensure proper error handling

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Emirhan Karcı**
- GitHub: [@emirhankarci](https://github.com/emirhankarci)
- LinkedIn: [Emirhan Karcı](https://linkedin.com/in/emirhankarci)

## 🙏 Acknowledgments

- **Lottie**: Amazing animation library by Airbnb
- **Firebase**: Reliable backend services by Google
- **Jetpack Compose**: Modern UI toolkit by Google
- **Material Design**: Beautiful design system by Google

## 🔮 Future Plans

- [ ] More country cuisines (Spain, Japan, Mexico)
- [ ] Video call integration during cooking
- [ ] Recipe sharing and rating system
- [ ] Grocery list generation
- [ ] Nutritional information
- [ ] Voice commands integration
- [ ] Smart kitchen device integration

---

**Made with ❤️ for couples who love cooking together**

*"Love that cooks in the same kitchen, lives in the same heart"* 💕
