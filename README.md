# GhostBarber

GhostBarber is an Android-based barber and grooming marketplace app that combines barbershop discovery, service booking, product marketplace, social posting, short-form reels, seller/admin panels, wallet flow, and an AI-assisted hairstyle preview concept.

The project demonstrates a mobile-first lifestyle platform for the barber industry, connecting customers, barbers, sellers, and content creators inside one Android application.

---

## Overview

GhostBarber is designed as a hybrid mobile platform for grooming and barber-related services.

The app combines several product ideas into one ecosystem:

- A barbershop and barber discovery experience
- A booking flow for barber services
- A marketplace for grooming-related products
- A social media-style content feed
- Short video reels
- User profile and account management
- Seller and admin management panels
- AI-assisted hairstyle preview concept
- Firebase-backed authentication, storage, and data handling

This repository is suitable as a portfolio project showing Android native development, Firebase integration, media handling, marketplace logic, social interaction flows, and AI-related feature integration.

---

## Main Features

### Authentication

- User login flow
- Firebase Authentication integration
- Auth-based navigation handling
- Redirect logic for logged-in and non-logged-in users

### Home & Discovery

- Home screen
- Carousel content support
- Search UI component
- Barbershop discovery
- Barberman discovery
- Academy / tips content sections

### Barbershop & Barber Booking

- Barbershop listing
- Barberman profile flow
- Service listing
- Booking page
- Barber request item layout
- Barberman panel for barber-side management

### Marketplace

- Shop page
- Product listing
- Product detail support
- Add product screen
- Cart page
- Seller panel
- Product model support

### Social Media Features

- Post model support
- Create post page
- Post item layout
- Comment dialog
- Comments page
- Notifications page
- Message page
- User profile page

### Reels & Media

- Reels page
- Reel item layout
- Video player activity
- Media playback support using ExoPlayer
- Video/media processing support

### AI Hairstyle Preview

- Hairstyle page
- Hairstyle item layout
- API key build configuration support
- Designed to support AI-assisted face or hairstyle transformation workflow

### Profile & Settings

- Profile page
- Edit profile page
- User setting page
- Wallet page
- Dashboard page

### Admin & Seller Tools

- Admin panel activity
- Seller panel page
- Barberman panel page
- Add product flow
- Add service dialog

---

## Tech Stack

### Mobile

- Kotlin
- Android SDK
- AndroidX
- Material Components
- ViewBinding
- DataBinding
- Navigation Component
- Safe Args
- Lifecycle ViewModel
- LiveData
- RecyclerView
- ConstraintLayout

### Backend & Cloud Services

- Firebase Authentication
- Cloud Firestore
- Firebase Storage
- Firebase Analytics
- Google Services Plugin

### Networking & API

- Retrofit
- OkHttp
- Gson
- BuildConfig API key support

### Media & Image Handling

- Glide
- ExoPlayer
- Firebase Storage
- Image Compressor
- FFmpeg Mobile

### Permissions & Utilities

- Dexter permission handler
- Kotlin Coroutines
- Google Play Services Auth

---

## Project Structure

```txt
ghostbarber/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── gbdev/
│   │       │           └── ghostbarber/
│   │       │               ├── models/
│   │       │               │   ├── Post.kt
│   │       │               │   └── Product.kt
│   │       │               ├── ui/
│   │       │               │   ├── academy/
│   │       │               │   ├── admin/
│   │       │               │   ├── barberman/
│   │       │               │   ├── barbershop/
│   │       │               │   ├── book/
│   │       │               │   ├── common/
│   │       │               │   ├── createpost/
│   │       │               │   ├── dashboard/
│   │       │               │   ├── hairstyle/
│   │       │               │   ├── home/
│   │       │               │   ├── message/
│   │       │               │   ├── more/
│   │       │               │   ├── notifications/
│   │       │               │   ├── profile/
│   │       │               │   ├── reels/
│   │       │               │   ├── seller/
│   │       │               │   ├── shop/
│   │       │               │   ├── tips/
│   │       │               │   ├── usersetting/
│   │       │               │   └── wallet/
│   │       │               ├── utils/
│   │       │               └── MainActivity.kt
│   │       └── res/
│   │           ├── layout/
│   │           └── navigation/
│   └── build.gradle.kts
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

---

## UI Modules

The application contains multiple UI modules for customer-facing, seller-facing, and admin-facing flows:

- `academy` — learning or academy-style content
- `admin` — admin panel features
- `barberman` — barber profile and barber-side features
- `barbershop` — barbershop discovery and profile flow
- `book` — service booking flow
- `createpost` — content creation flow
- `dashboard` — user dashboard
- `hairstyle` — hairstyle preview / AI-assisted hairstyle feature
- `home` — main discovery screen
- `message` — messaging page
- `notifications` — notification page
- `profile` — user profile and profile editing
- `reels` — short-form video content
- `seller` — seller panel
- `shop` — marketplace shop
- `tips` — tips or grooming content
- `usersetting` — user settings
- `wallet` — wallet-related page

---

## Layout Highlights

The project includes layouts for:

- Login
- Home
- Dashboard
- Profile
- Edit profile
- Barbershop
- Barberman
- Barberman panel
- Booking
- Shop
- Cart
- Seller panel
- Admin panel
- Product item
- Add product
- Add service
- Create post
- Post item
- Comment dialog
- Comments
- Notifications
- Message
- Reels
- Reel item
- Video player
- Hairstyle
- Hairstyle item
- Wallet
- Tips
- Academy

---

## Getting Started

### Prerequisites

Make sure you have installed:

- Android Studio
- JDK 8 or newer
- Android SDK
- Gradle support
- Firebase project
- Android emulator or physical Android device

---

## Installation

Clone the repository:

```bash
git clone https://github.com/iraroniyuda/ghostbarber.git
cd ghostbarber
```

Open the project in Android Studio.

Sync Gradle dependencies, then run:

```bash
./gradlew build
```

For Windows:

```bash
gradlew.bat build
```

---

## Firebase Setup

This project uses Firebase services.

Recommended setup:

1. Create a Firebase project.
2. Register an Android app with the package name:

```txt
com.gbdev.ghostbarber
```

3. Enable Firebase Authentication.
4. Enable Cloud Firestore.
5. Enable Firebase Storage.
6. Download `google-services.json`.
7. Place the file inside:

```txt
app/google-services.json
```

---

## API Key Configuration

The app is configured to read an `API_KEY` value through `BuildConfig`.

Add your API key to the appropriate Gradle/local properties configuration used by the project.

Example:

```properties
API_KEY=your_api_key_here
```

Do not commit private API keys or production credentials to the repository.

---

## Running the App

Build debug APK:

```bash
./gradlew assembleDebug
```

Install debug APK to a connected device:

```bash
./gradlew installDebug
```

Or run directly from Android Studio using an emulator or physical device.

---

## Security Notes

This app may involve user accounts, profile data, media uploads, marketplace data, and external API usage.

For production usage, make sure to secure:

- Firebase Authentication rules
- Firestore security rules
- Firebase Storage rules
- API key restrictions
- Admin-only access
- Seller-only access
- User-uploaded media validation
- Content moderation flow
- Sensitive environment values

Never commit:

- `google-services.json` containing production credentials
- Private API keys
- Admin credentials
- Service account keys
- Production secrets

---

## Current Status

This repository is a portfolio implementation of an Android barber marketplace and social platform concept.

Some production-level setup may require additional configuration, including:

- Firebase project setup
- Firestore data structure
- Firebase security rules
- Storage rules
- External AI API integration
- API key configuration
- Production app signing
- Content moderation workflow
- Payment and order handling, if marketplace checkout is expanded

---

## Suggested Improvements

Recommended improvements for future development:

- Add screenshots for home, booking, shop, reels, profile, and hairstyle pages
- Add demo video or GIF preview
- Add Firebase schema documentation
- Add Firestore rules documentation
- Add AI hairstyle API integration notes
- Add marketplace checkout flow documentation
- Add seller/admin permission documentation
- Add architecture diagram
- Add release APK link
- Add test documentation
- Add CI/CD build workflow

---



## Author

Developed by Ira Roni Yuda.

This project was built as an Android implementation for a barber marketplace, booking platform, social content experience, and AI-assisted hairstyle preview concept.
