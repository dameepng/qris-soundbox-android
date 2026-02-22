const { getFirebaseAdmin } = require('../config/firebase')

/**
 * Send payment notification to merchant Android device
 */
const sendPaymentNotification = async ({
  fcmToken,
  transactionId,
  orderId,
  amount,
  status,
  customerName = ''
}) => {
  try {
    const admin = getFirebaseAdmin()

    const message = {
      token: fcmToken,

      // Data payload (for background handling in FCMService.kt)
      data: {
        type: 'payment',
        transaction_id: transactionId,
        order_id: orderId || '',
        amount: amount.toString(),
        status: status,
        customer_name: customerName || '',
        timestamp: new Date().toISOString()
      },

      // Android specific config
      android: {
        priority: 'high',
        ttl: 60000, // 60 seconds
        notification: {
          channelId: 'payment_channel',
          priority: 'high',
          defaultSound: true,
          defaultVibrateTimings: true
        }
      }
    }

    const response = await admin.messaging().send(message)
    console.log(`✅ FCM sent successfully: ${response}`)
    return { success: true, messageId: response }

  } catch (error) {
    console.error(`❌ FCM send failed: ${error.message}`)
    return { success: false, error: error.message }
  }
}

/**
 * Send FCM to multiple devices
 */
const sendPaymentNotificationMultiple = async (tokens, payload) => {
  try {
    const admin = getFirebaseAdmin()

    const message = {
      tokens: tokens,
      data: payload,
      android: {
        priority: 'high',
        ttl: 60000
      }
    }

    const response = await admin.messaging().sendEachForMulticast(message)
    console.log(`✅ FCM multicast: ${response.successCount} success, ${response.failureCount} failed`)
    return response

  } catch (error) {
    console.error(`❌ FCM multicast failed: ${error.message}`)
    throw error
  }
}

module.exports = {
  sendPaymentNotification,
  sendPaymentNotificationMultiple
}