# English Master AI — Architecture, Installation & Deployment Guide

## 1. Architecture Overview
- **Mobile & Responsive Adaptive Client**: Built with Kotlin, Jetpack Compose Material 3, adaptive layouts, and full multilingual support (`Français`, `العربية` with automatic RTL layout direction, and `English`).
- **Cloud Database & Real-Time Sync**: Provisioned with **Firebase Firestore Enterprise Edition** (`firebase-blueprint.json` + zero-trust `firestore.rules`) and **Google Sign-In via Jetpack Credential Manager**. Also includes the complete 29-table **Supabase PostgreSQL + RLS SQL schema** in `/docs/SUPABASE_SCHEMA.sql`.
- **Server-Attested AI Engine**: Powered by **Firebase AI Logic (`gemini-3.8-flash`)** protected by **Firebase App Check** (no client-side API key exposed in frontend code).
- **Offline Mode (`Room`)**: Local SQLite persistence (`OfflineDatabase`) storing downloaded lesson packs and an offline score queue that automatically synchronizes to the cloud when connectivity resumes.
- **Audio & Speech Engine**: Native Android `TextToSpeech` (with UK/US accent switching and adjustable `0.75x`–`1.5x` playback speed) and `SpeechRecognizer` voice recognition with pronunciation fluency analysis.

## 2. Environment Setup & Secrets
1. `.env.example` contains template placeholders without any secret keys.
2. Firebase AI Logic and App Check are automatically provisioned via `app/google-services.json` and `app/src/main/res/values/firebase_applet_config.xml`.
3. **Billing / Subscriptions (Section 12)**: In compliance with strict production rules, **payments are never simulated as successful**. Selecting a paid plan (`Premium`, `AI Pro`, `Family`) displays the transparent configuration notice requiring Google Play Billing / Stripe live webhook credentials in the backend secrets before charging any user.

## 3. Running Tests & Verification
- **Firestore Security Rules (JavaScript Tier 1)**:
  ```bash
  FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
  ```
- **Android Repository & Security Rules Integration (Robolectric Tier 2)**:
  ```bash
  gradle :app:testDebugUnitTest --tests '*Rule*'
  ```
- **Build Debug APK**:
  ```bash
  gradle assembleDebug
  ```
