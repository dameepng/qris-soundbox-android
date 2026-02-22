const { validateWebhookSignature } = require('../services/midtransService')
const { pool } = require('../config/database')

const validateMidtransWebhook = async (req, res, next) => {
  try {
    const payload = req.body

    console.log('📥 Webhook received:', JSON.stringify(payload, null, 2))

    const {
      order_id,
      status_code,
      gross_amount,
      signature_key
    } = payload

    // Validate signature FIRST (before saving to DB)
    const isValid = validateWebhookSignature(
      order_id,
      status_code,
      gross_amount,
      signature_key
    )

    // Log webhook with validation result
    await pool.query(
      `INSERT INTO webhook_logs
       (order_id, payload, signature, is_valid)
       VALUES ($1, $2, $3, $4)`,
      [order_id, JSON.stringify(payload), signature_key, isValid]
    )

    if (!isValid) {
      console.warn(`⚠️ Invalid webhook signature for order: ${order_id}`)
      return res.status(401).json({
        success: false,
        error: 'Invalid signature'
      })
    }

    console.log(`✅ Webhook signature valid for order: ${order_id}`)
    next()

  } catch (error) {
    console.error('Webhook validation error:', error)
    // Still return 200 to Midtrans to prevent retries
    return res.status(200).json({ success: false })
  }
}

module.exports = { validateMidtransWebhook }