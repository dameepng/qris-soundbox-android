const midtransClient = require('midtrans-client')
const crypto = require('crypto')

// Initialize Midtrans Core API
const getCoreApi = () => {
  return new midtransClient.CoreApi({
    isProduction: process.env.MIDTRANS_IS_PRODUCTION === 'true',
    serverKey: process.env.MIDTRANS_SERVER_KEY,
    clientKey: process.env.MIDTRANS_CLIENT_KEY
  })
}

/**
 * Generate QRIS via Core API (Direct QR String)
 * Docs: https://docs.midtrans.com/reference/charge-qris
 */
const generateQRISCoreApi = async ({ orderId, amount, merchantId }) => {
  try {
    const core = getCoreApi()

    const parameter = {
      payment_type: 'qris',
      transaction_details: {
        order_id: orderId,
        gross_amount: amount
      },
      qris: {
        acquirer: 'gopay' // or 'airpay indonesia'
      }
    }

    console.log('🔄 Generating QRIS via Midtrans Core API:', orderId)
    const charge = await core.charge(parameter)

    console.log('✅ Midtrans response:', JSON.stringify(charge, null, 2))

    // Extract QRIS data from response
    // Response structure:
    // {
    //   status_code: '201',
    //   status_message: 'QRIS transaction is created',
    //   transaction_id: 'xxx',
    //   order_id: 'xxx',
    //   merchant_id: 'xxx',
    //   gross_amount: '10000',
    //   currency: 'IDR',
    //   payment_type: 'qris',
    //   transaction_time: '2024-01-01 10:00:00',
    //   transaction_status: 'pending',
    //   fraud_status: 'accept',
    //   actions: [
    //     {
    //       name: 'generate-qr-code',
    //       method: 'GET',
    //       url: 'https://api.sandbox.midtrans.com/v2/qris/xxx/qr-code'
    //     }
    //   ],
    //   expiry_time: '2024-01-01 10:05:00'
    // }

    if (charge.status_code === '201' || charge.status_code === '200') {
      // Find QR code URL from actions
      const qrAction = charge.actions?.find(a => a.name === 'generate-qr-code')
      const qrCodeUrl = qrAction?.url

      return {
        success: true,
        transactionId: charge.transaction_id,
        orderId: charge.order_id,
        qrisString: charge.qr_string || qrCodeUrl, // QR string atau URL to QR image
        qrisUrl: qrCodeUrl,
        amount: parseInt(charge.gross_amount),
        expiryTime: charge.expiry_time,
        status: charge.transaction_status
      }
    } else {
      return {
        success: false,
        error: charge.status_message || 'Failed to generate QRIS'
      }
    }

  } catch (error) {
    console.error('❌ Midtrans Core API error:', error.message)

    // Log full error for debugging
    if (error.ApiResponse) {
      console.error('Midtrans API Response:', JSON.stringify(error.ApiResponse, null, 2))
    }

    return {
      success: false,
      error: error.message,
      details: error.ApiResponse || null
    }
  }
}

/**
 * Validate Midtrans webhook signature
 */
const validateWebhookSignature = (orderId, statusCode, grossAmount, receivedSignature) => {
  const serverKey = process.env.MIDTRANS_SERVER_KEY
  const rawString = `${orderId}${statusCode}${grossAmount}${serverKey}`

  const expectedSignature = crypto
    .createHash('sha512')
    .update(rawString)
    .digest('hex')

  return expectedSignature === receivedSignature
}

/**
 * Check transaction status from Midtrans
 */
const checkTransactionStatus = async (orderId) => {
  try {
    const core = getCoreApi()
    const status = await core.transaction.status(orderId)
    return { success: true, data: status }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

/**
 * Cancel/Expire transaction
 */
const cancelTransaction = async (orderId) => {
  try {
    const core = getCoreApi()
    const result = await core.transaction.cancel(orderId)
    return { success: true, data: result }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

module.exports = {
  generateQRISCoreApi,
  validateWebhookSignature,
  checkTransactionStatus,
  cancelTransaction
}