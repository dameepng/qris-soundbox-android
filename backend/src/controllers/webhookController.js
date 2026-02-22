const { pool } = require('../config/database')
const { sendPaymentNotification } = require('../services/fcmService')
const { MIDTRANS_STATUS, STATUS, FCM } = require('../utils/constants')

/**
 * POST /webhook/payment (from Midtrans)
 */
const handleMidtransWebhook = async (req, res) => {
  // Always respond 200 first to prevent Midtrans retry
  res.status(200).json({ success: true })

  try {
    const payload = req.body
    const {
      order_id,
      transaction_id,
      transaction_status,
      fraud_status,
      gross_amount,
      payment_type,
      transaction_time
    } = payload

    console.log(`📥 Webhook received: ${order_id} - ${transaction_status}`)

    // Only process successful payments
    const isSuccess = (
      transaction_status === MIDTRANS_STATUS.SETTLEMENT ||
      (transaction_status === MIDTRANS_STATUS.CAPTURE && fraud_status === 'accept')
    )

    if (!isSuccess) {
      console.log(`⏭️ Skipping non-success status: ${transaction_status}`)

      // Update QRIS status for failed/expired
      if ([MIDTRANS_STATUS.EXPIRE, MIDTRANS_STATUS.CANCEL, MIDTRANS_STATUS.DENY].includes(transaction_status)) {
        await pool.query(
          `UPDATE qris_transactions SET status = 'expired' WHERE order_id = $1`,
          [order_id]
        )
      }
      return
    }

    // Get QRIS transaction to find merchant
    const qrisResult = await pool.query(
      'SELECT * FROM qris_transactions WHERE order_id = $1',
      [order_id]
    )

    if (qrisResult.rows.length === 0) {
      console.warn(`⚠️ QRIS not found for order: ${order_id}`)
      return
    }

    const qrisTransaction = qrisResult.rows[0]
    const merchantId = qrisTransaction.merchant_id
    const amount = parseInt(gross_amount)

    // Check if already processed (prevent duplicate)
    if (qrisTransaction.status === STATUS.SUCCESS) {
      console.log(`⏭️ Already processed: ${order_id}`)
      return
    }

    // Get merchant FCM token
    const merchantResult = await pool.query(
      'SELECT * FROM merchants WHERE merchant_id = $1',
      [merchantId]
    )

    if (merchantResult.rows.length === 0) {
      console.error(`❌ Merchant not found: ${merchantId}`)
      return
    }

    const merchant = merchantResult.rows[0]

    // 1. Save transaction to database
    await pool.query(
      `INSERT INTO transactions
       (transaction_id, merchant_id, order_id, amount, status, payment_method, paid_at, raw_data)
       VALUES ($1, $2, $3, $4, $5, $6, NOW(), $7)
       ON CONFLICT (transaction_id) DO NOTHING`,
      [
        transaction_id,
        merchantId,
        order_id,
        amount,
        STATUS.SUCCESS,
        payment_type || 'qris',
        JSON.stringify(payload)
      ]
    )

    // 2. Update QRIS transaction status
    await pool.query(
      `UPDATE qris_transactions
       SET status = 'paid', paid_at = NOW(), transaction_id = $1
       WHERE order_id = $2`,
      [transaction_id, order_id]
    )

    // 3. Update webhook log - FIXED: Use subquery instead of ORDER BY
    await pool.query(
      `UPDATE webhook_logs
       SET processed = true
       WHERE id = (
         SELECT id FROM webhook_logs
         WHERE order_id = $1
         ORDER BY created_at DESC
         LIMIT 1
       )`,
      [order_id]
    )

    // 4. Send FCM notification to merchant
    if (merchant.fcm_token) {
      const fcmResult = await sendPaymentNotification({
        fcmToken: merchant.fcm_token,
        transactionId: transaction_id,
        orderId: order_id,
        amount: amount,
        status: STATUS.SUCCESS,
        customerName: payload.customer_details?.first_name || ''
      })

      if (fcmResult.success) {
        console.log(`✅ Payment processed & FCM sent: ${order_id} - Rp ${amount}`)
      } else {
        console.error(`⚠️ Payment processed but FCM failed: ${fcmResult.error}`)
      }
    } else {
      console.warn(`⚠️ No FCM token for merchant: ${merchantId}`)
    }

  } catch (error) {
    console.error('Webhook processing error:', error)
    // Don't send error response - already sent 200
  }
}

/**
 * POST /webhook/test - For testing without Midtrans (Development only)
 */
const handleTestWebhook = async (req, res) => {
  if (process.env.NODE_ENV === 'production') {
    return res.status(404).json({ error: 'Not found' })
  }

  try {
    const { merchant_id, amount = 50000 } = req.body

    // Get merchant
    const merchantResult = await pool.query(
      'SELECT * FROM merchants WHERE merchant_id = $1',
      [merchant_id]
    )

    if (merchantResult.rows.length === 0) {
      return res.status(404).json({ error: 'Merchant not found' })
    }

    const merchant = merchantResult.rows[0]
    const transactionId = `TEST-${Date.now()}`
    const orderId = `TEST-ORDER-${Date.now()}`

    // Save test transaction
    await pool.query(
      `INSERT INTO transactions
       (transaction_id, merchant_id, order_id, amount, status, payment_method, paid_at)
       VALUES ($1, $2, $3, $4, 'success', 'qris', NOW())`,
      [transactionId, merchant_id, orderId, amount]
    )

    // Send FCM
    if (merchant.fcm_token) {
      await sendPaymentNotification({
        fcmToken: merchant.fcm_token,
        transactionId,
        orderId,
        amount,
        status: 'success',
        customerName: 'Test Customer'
      })
    }

    console.log(`✅ Test webhook sent to merchant: ${merchant_id}`)

    return res.json({
      success: true,
      message: 'Test notification sent',
      data: { transactionId, orderId, amount }
    })

  } catch (error) {
    console.error('Test webhook error:', error)
    return res.status(500).json({ error: error.message })
  }
}

module.exports = {
  handleMidtransWebhook,
  handleTestWebhook
}