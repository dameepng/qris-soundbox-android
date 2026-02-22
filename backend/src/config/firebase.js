const admin = require('firebase-admin')

let isInitialized = false

const initFirebase = () => {
  if (isInitialized) return

  try {
    // Parse private key (Railway stores as escaped string)
    const privateKey = process.env.FIREBASE_PRIVATE_KEY
      ? process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
      : undefined

    admin.initializeApp({
      credential: admin.credential.cert({
        projectId: process.env.FIREBASE_PROJECT_ID,
        privateKeyId: process.env.FIREBASE_PRIVATE_KEY_ID,
        privateKey: privateKey,
        clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
        clientId: process.env.FIREBASE_CLIENT_ID,
        authUri: 'https://accounts.google.com/o/oauth2/auth',
        tokenUri: 'https://oauth2.googleapis.com/token'
      })
    })

    isInitialized = true
    console.log('✅ Firebase Admin initialized')

  } catch (error) {
    console.error('❌ Firebase init error:', error.message)
    throw error
  }
}

const getFirebaseAdmin = () => {
  if (!isInitialized) {
    initFirebase()
  }
  return admin
}

module.exports = { initFirebase, getFirebaseAdmin }