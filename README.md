# QRIS Soundbox

QRIS Soundbox is a comprehensive payment notification system that integrates an Android application with a Node.js backend to provide real-time audio and visual alerts for QRIS transactions. It uses Midtrans as the payment gateway and Firebase Cloud Messaging (FCM) for instant notifications.

## Project Structure

This repository contains two main components:
1. **Android Application**: Built with Jetpack Compose, located in the `app` directory.
2. **Backend Server**: Built with Express.js and PostgreSQL, located in the `backend` directory.

## Key Features

- Real-time payment notifications via Soundbox.
- QRIS dynamic QR code generation.
- Integration with Midtrans Payment Gateway.
- Push notifications using Firebase Cloud Messaging (FCM).
- Merchant management system.
- Transaction history and status tracking.
- Secure API with Helmet and CORS protection.

## Technology Stack

### Android Application
- Kotlin and Jetpack Compose for UI.
- Retrofit and OkHttp for API communication.
- Room Database for local data persistence.
- Firebase Cloud Messaging for notifications.
- ZXing for QR code handling.
- WorkManager for background tasks.
- Navigation Compose for screen routing.

### Backend Server
- Node.js and Express.js framework.
- PostgreSQL database with `pg` client.
- Midtrans Node.js SDK for payment processing.
- Firebase Admin SDK for push notifications.
- CryptoJS for secure data handling.
- Morgan for request logging.
- Helmet for security headers and CORS for resource sharing.

## Prerequisites

- Android Studio Koala or newer.
- Node.js version 18.0.0 or higher.
- PostgreSQL database instance.
- Firebase Project with CM enabled.
- Midtrans Account (Sandbox or Production).

## Installation and Setup

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a `.env` file based on `.env.example`:
   ```bash
   cp .env.example .env
   ```
4. Configure your environment variables (Database URL, Midtrans Keys, Firebase Credentials).
5. Start the server:
   ```bash
   npm run dev
   ```

### Android App Setup
1. Open the project in Android Studio.
2. Ensure you have the `google-services.json` file in the `app/` directory.
3. Update the base URL in the app's configuration to point to your backend server.
4. Sync Gradle and run the application on an emulator or physical device.

## Backend Environment Variables

Key variables required in your `.env` file:
- `PORT`: Server port (default 3000).
- `DATABASE_URL`: PostgreSQL connection string.
- `MIDTRANS_SERVER_KEY`: Your Midtrans server key.
- `MIDTRANS_IS_PRODUCTION`: Set to `true` for production, `false` for sandbox.
- `FIREBASE_SERVICE_ACCOUNT`: Path to your Firebase service account JSON file.

## Deployment

The backend is configured for deployment on Railway using Nixpacks. Refer to `railway.json` and `nixpacks.toml` for specific deployment settings.

## License

This project is licensed under the terms of the MIT license.
