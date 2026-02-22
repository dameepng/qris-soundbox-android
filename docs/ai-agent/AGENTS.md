# AI Agent Instructions for Soundbox QRIS Android Development

## Project Overview
You are an AI coding assistant helping to build **Soundbox QRIS**, an Android application that provides real-time audio notifications for QRIS payment transactions. This is a production-grade application for Indonesian merchants (UMKM).

## Core Principles

### 1. **NO HALLUCINATION RULES**
- ✅ **ONLY use libraries and APIs explicitly listed in this document**
- ✅ **ONLY reference code that has been previously created in this session**
- ✅ **NEVER invent package names, class names, or method names**
- ✅ **ALWAYS verify version compatibility before suggesting code**
- ✅ **ASK for clarification if requirements are ambiguous**
- ❌ **DO NOT assume features exist without verification**
- ❌ **DO NOT create code that depends on non-existent files**
- ❌ **DO NOT suggest outdated or deprecated approaches**

### 2. **Code Generation Rules**
- Every code snippet must be **complete and compilable**
- Include all necessary imports
- Add error handling for every external call
- Include TODO comments for incomplete sections
- Follow Kotlin coding conventions strictly
- Use type-safe builders where applicable

### 3. **Verification Checklist**
Before generating any code, verify:
- [ ] All dependencies are in the approved tech stack
- [ ] All referenced classes/methods exist in Android SDK or approved libraries
- [ ] All file paths follow the project structure
- [ ] All API endpoints match the documented contract
- [ ] Code follows MVVM architecture pattern

---

## Project Structure (SINGLE SOURCE OF TRUTH)
````
soundbox-qris/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/soundbox/qris/
│   │   │   │   ├── SoundboxApplication.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── TransactionDao.kt
│   │   │   │   │   │   │   ├── MerchantDao.kt
│   │   │   │   │   │   │   └── QRISDao.kt
│   │   │   │   │   │   ├── database/
│   │   │   │   │   │   │   └── SoundboxDatabase.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── Transaction.kt
│   │   │   │   │   │       ├── MerchantSettings.kt
│   │   │   │   │   │       └── QRISHistory.kt
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   │   └── SoundboxApi.kt
│   │   │   │   │   │   └── dto/
│   │   │   │   │   │       ├── QRISRequest.kt
│   │   │   │   │   │       ├── QRISResponse.kt
│   │   │   │   │   │       └── ApiResponse.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── TransactionRepository.kt
│   │   │   │   │       ├── MerchantRepository.kt
│   │   │   │   │       └── QRISRepository.kt
│   │   │   │   ├── service/
│   │   │   │   │   ├── FCMService.kt
│   │   │   │   │   └── TTSService.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── main/
│   │   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   │   ├── MainViewModel.kt
│   │   │   │   │   │   └── TransactionAdapter.kt
│   │   │   │   │   ├── qris/
│   │   │   │   │   │   ├── QRISGeneratorFragment.kt
│   │   │   │   │   │   └── QRISViewModel.kt
│   │   │   │   │   ├── settings/
│   │   │   │   │   │   ├── SettingsActivity.kt
│   │   │   │   │   │   └── SettingsViewModel.kt
│   │   │   │   │   └── splash/
│   │   │   │   │       └── SplashActivity.kt
│   │   │   │   └── utils/
│   │   │   │       ├── Constants.kt
│   │   │   │       ├── Extensions.kt
│   │   │   │       ├── NotificationHelper.kt
│   │   │   │       └── CurrencyFormatter.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   ├── drawable/
│   │   │   │   └── raw/
│   │   │   │       ├── payment_success.mp3
│   │   │   │       └── beep.mp3
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle.kts
│   └── google-services.json
└── build.gradle.kts
````

**RULE: Never create files outside this structure without explicit approval.**

---

## Approved Tech Stack (EXHAUSTIVE LIST)

### Android Dependencies (build.gradle.kts)
````kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Firebase (BOM ensures version compatibility)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // QR Code generation
    implementation("com.google.zxing:core:3.5.2")
    
    // WorkManager (for background tasks)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
````

**RULE: NEVER suggest dependencies not in this list. If new dependency needed, request approval first.**

---

## API Contract (EXACT SPECIFICATION)

### Base URL
````
Development: https://soundbox-api-staging.railway.app
Production: https://soundbox-api.railway.app
````

### Authentication
````
Header: X-API-Key: {merchant_api_key}
````

### Endpoints (COMPLETE SPECIFICATION)

#### 1. Generate Dynamic QRIS
````http
POST /api/qris/generate
Content-Type: application/json
X-API-Key: {api_key}

Request Body:
{
  "merchant_id": "string",      // Required, max 50 chars
  "amount": integer,            // Required, min 1000, max 10000000
  "description": "string"       // Optional, max 200 chars
}

Success Response (200):
{
  "success": true,
  "data": {
    "qris_id": "string",        // Format: QRIS-{timestamp}
    "order_id": "string",       // Format: ORDER-{timestamp}-{merchant_id}
    "amount": integer,
    "qris_string": "string",    // Actual QRIS data for QR generation
    "expires_at": "ISO8601",    // Timestamp, 5 minutes from creation
    "status": "pending"
  }
}

Error Response (400):
{
  "success": false,
  "error": "string",            // Human-readable error message
  "code": "string"              // Error code: INVALID_AMOUNT, MERCHANT_NOT_FOUND, etc.
}
````

#### 2. Check QRIS Status
````http
GET /api/qris/status/{order_id}
X-API-Key: {api_key}

Success Response (200):
{
  "success": true,
  "data": {
    "order_id": "string",
    "status": "string",         // "pending" | "paid" | "expired"
    "amount": integer,
    "created_at": "ISO8601",
    "paid_at": "ISO8601"        // null if not paid
  }
}
````

#### 3. Register Merchant
````http
POST /api/merchant/register
Content-Type: application/json

Request Body:
{
  "merchant_id": "string",      // Required, unique
  "merchant_name": "string",    // Required
  "fcm_token": "string",        // Required
  "phone_number": "string"      // Optional
}

Success Response (200):
{
  "success": true,
  "data": {
    "merchant_id": "string",
    "api_key": "string",        // Save this securely
    "qris_static": "string"     // Static QRIS code
  }
}
````

#### 4. Update FCM Token
````http
PUT /api/merchant/fcm-token
Content-Type: application/json
X-API-Key: {api_key}

Request Body:
{
  "merchant_id": "string",
  "fcm_token": "string"
}

Success Response (200):
{
  "success": true,
  "message": "FCM token updated"
}
````

**RULE: These are the ONLY endpoints available. Never invent new endpoints.**

---

## FCM Message Format (EXACT SCHEMA)
````json
{
  "data": {
    "type": "payment",
    "transaction_id": "string",
    "order_id": "string",
    "amount": "string",           // String representation of integer
    "status": "success",          // "success" | "pending" | "failed"
    "payment_method": "qris",
    "customer_name": "string",    // May be empty
    "timestamp": "ISO8601"
  }
}
````

**RULE: FCMService must ONLY handle messages with this exact structure.**

---

## Database Schemas (EXACT DEFINITIONS)

### Transaction Entity
````kotlin
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val transactionId: String,
    
    val amount: Int,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "qris",
    
    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "order_id")
    val orderId: String? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)
````

### MerchantSettings Entity
````kotlin
@Entity(tableName = "merchant_settings")
data class MerchantSettings(
    @PrimaryKey
    val merchantId: String,
    
    @ColumnInfo(name = "fcm_token")
    val fcmToken: String,
    
    @ColumnInfo(name = "api_key")
    val apiKey: String,
    
    @ColumnInfo(name = "qris_static")
    val qrisStatic: String,
    
    @ColumnInfo(name = "merchant_name")
    val merchantName: String,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "tts_enabled")
    val ttsEnabled: Boolean = true,
    
    @ColumnInfo(name = "tts_volume")
    val ttsVolume: Float = 1.0f,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
````

### QRISHistory Entity
````kotlin
@Entity(tableName = "qris_history")
data class QRISHistory(
    @PrimaryKey
    val qrisId: String,
    
    @ColumnInfo(name = "order_id")
    val orderId: String,
    
    val amount: Int,
    
    @ColumnInfo(name = "qris_string")
    val qrisString: String,
    
    val status: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long,
    
    @ColumnInfo(name = "paid_at")
    val paidAt: Long? = null,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null
)
````

**RULE: These are the ONLY database entities. Never add fields without approval.**

---

## Android Component Usage Rules

### TextToSpeech (EXACT USAGE)
````kotlin
// Initialization
val tts = TextToSpeech(context) { status ->
    if (status == TextToSpeech.SUCCESS) {
        tts.language = Locale("id", "ID")
        tts.setSpeechRate(0.85f)
        tts.setPitch(1.0f)
    }
}

// Speaking (correct method signature)
tts.speak(
    text: String,
    queueMode: Int,  // QUEUE_FLUSH or QUEUE_ADD
    params: Bundle?,
    utteranceId: String?
)

// NEVER use deprecated speak() method with HashMap
````

### FCM Service (EXACT IMPLEMENTATION)
````kotlin
class FCMService : FirebaseMessagingService() {
    
    // This method is called when app receives FCM message
    override fun onMessageReceived(message: RemoteMessage) {
        // message.data is Map<String, String>
        // NEVER access message.notification for data messages
    }
    
    // This method is called when FCM token is refreshed
    override fun onNewToken(token: String) {
        // Send token to server
    }
}
````

### Wake Lock (EXACT USAGE)
````kotlin
val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
val wakeLock = powerManager.newWakeLock(
    PowerManager.PARTIAL_WAKE_LOCK,
    "Soundbox::TTS"
)

wakeLock.acquire(10_000) // Max 10 seconds
try {
    // Do work
} finally {
    if (wakeLock.isHeld) {
        wakeLock.release()
    }
}
````

**RULE: Use ONLY these exact patterns. No variations.**

---

## Constants (SINGLE SOURCE OF TRUTH)
````kotlin
object Constants {
    // API
    const val BASE_URL_DEV = "https://soundbox-api-staging.railway.app/"
    const val BASE_URL_PROD = "https://soundbox-api.railway.app/"
    
    // FCM
    const val FCM_CHANNEL_ID = "payment_channel"
    const val FCM_CHANNEL_NAME = "Payment Notifications"
    
    // QRIS
    const val QRIS_EXPIRY_MINUTES = 5
    const val QRIS_MIN_AMOUNT = 1000
    const val QRIS_MAX_AMOUNT = 10_000_000
    
    // TTS
    const val TTS_SPEECH_RATE = 0.85f
    const val TTS_PITCH = 1.0f
    const val TTS_WAKE_LOCK_TIMEOUT = 10_000L // 10 seconds
    
    // Database
    const val DATABASE_NAME = "soundbox_db"
    const val DATABASE_VERSION = 1
    
    // Shared Preferences
    const val PREF_NAME = "soundbox_prefs"
    const val PREF_MERCHANT_ID = "merchant_id"
    const val PREF_API_KEY = "api_key"
    const val PREF_FCM_TOKEN = "fcm_token"
}
````

**RULE: All magic strings/numbers must come from Constants. No hardcoded values.**

---

## Error Handling Patterns (MANDATORY)

### Repository Pattern
````kotlin
suspend fun generateQRIS(amount: Int): Result<QRISData> {
    return try {
        val response = api.generateQRIS(QRISRequest(merchantId, amount))
        if (response.success) {
            Result.success(response.data)
        } else {
            Result.failure(ApiException(response.error))
        }
    } catch (e: IOException) {
        Result.failure(NetworkException("Network error", e))
    } catch (e: Exception) {
        Result.failure(UnknownException("Unknown error", e))
    }
}
````

### ViewModel Pattern
````kotlin
fun generateQRIS(amount: Int) {
    viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        
        repository.generateQRIS(amount)
            .onSuccess { data ->
                _qrisData.value = data
            }
            .onFailure { error ->
                _error.value = error.message ?: "Unknown error"
            }
        
        _isLoading.value = false
    }
}
````

**RULE: Every external call must have try-catch or Result wrapper.**

---

## Testing Requirements

### Unit Test Template
````kotlin
@Test
fun `generateQRIS with valid amount returns success`() = runTest {
    // Given
    val amount = 50000
    val mockResponse = QRISResponse(/* ... */)
    coEvery { api.generateQRIS(any()) } returns mockResponse
    
    // When
    val result = repository.generateQRIS(amount)
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(amount, result.getOrNull()?.amount)
}
````

**RULE: Every public function must have corresponding test.**

---

## Common Mistakes to AVOID

### ❌ NEVER DO THIS:
````kotlin
// Hardcoded values
val url = "https://api.example.com" // Use Constants.BASE_URL

// Deprecated APIs
tts.speak(text, QUEUE_FLUSH, null) // Missing utteranceId parameter

// Blocking main thread
val data = repository.getData() // Use coroutines

// Ignoring nullability
val name = user.name.toUpperCase() // Use user.name?.uppercase()

// Magic numbers
delay(5000) // Use named constant

// Non-exhaustive when
when (status) {
    "success" -> // handle
    // Missing else branch
}
````

### ✅ DO THIS INSTEAD:
````kotlin
// Named constants
val url = Constants.BASE_URL_PROD

// Current APIs
tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_id")

// Coroutines
viewModelScope.launch {
    val data = repository.getData()
}

// Safe calls
val name = user.name?.uppercase() ?: "Unknown"

// Named constants
delay(Constants.TTS_WAKE_LOCK_TIMEOUT)

// Exhaustive when
when (status) {
    "success" -> // handle
    "pending" -> // handle
    else -> // handle unknown
}
````

---

## Response Format (MANDATORY)

When generating code, always use this format:
````markdown
## File: `path/to/File.kt`
```kotlin
// Complete, compilable code here
```

**Dependencies:**
- androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2
- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

**Related Files:**
- `path/to/RelatedFile.kt` (must exist)
- `path/to/AnotherFile.kt` (must exist)

**TODO:**
- [ ] Add error handling for network timeouts
- [ ] Write unit tests

**Notes:**
- This implements MVVM pattern
- Follows repository pattern for data layer
````

---

## Validation Checklist (RUN BEFORE EVERY RESPONSE)

Before providing any code, verify:

- [ ] All imports are from approved dependencies
- [ ] All referenced classes exist in project structure
- [ ] All API calls match documented endpoints
- [ ] All database operations use defined entities
- [ ] Error handling is present
- [ ] No hardcoded values (use Constants)
- [ ] Follows Kotlin conventions
- [ ] No deprecated APIs used
- [ ] Null safety handled
- [ ] Coroutines used for async operations

---

## When in Doubt

If you're unsure about:
- **Dependency versions**: Ask for confirmation
- **API endpoints**: Refer to API Contract section
- **Database schema**: Use EXACT entities defined above
- **Android APIs**: Use official Android documentation for API 24+
- **Best practices**: Follow MVVM + Repository pattern

**NEVER guess or hallucinate. Always ask for clarification.**

---

## Escalation Protocol

If you encounter:
1. **Missing specification**: Ask user to provide details
2. **Conflicting requirements**: Point out conflict, ask for resolution
3. **Deprecated API needed**: Suggest modern alternative
4. **New dependency needed**: Request approval with justification

---

## Success Criteria

Your responses are successful when:
- ✅ Code compiles without errors
- ✅ All imports are valid
- ✅ Follows project structure exactly
- ✅ Uses only approved dependencies
- ✅ Includes proper error handling
- ✅ Matches API contracts precisely
- ✅ User can copy-paste and run immediately

---

**Remember: Precision over speed. Accuracy over assumptions.**