const { pool } = require('../config/database')
const { generateApiKey, successResponse, errorResponse } = require('../utils/helpers')
const { body, validationResult } = require('express-validator')

// Validation rules
const registerValidation = [
  body('merchant_id')
    .notEmpty().withMessage('merchant_id diperlukan')
    .isLength({ max: 50 }).withMessage('merchant_id max 50 karakter'),
  body('merchant_name')
    .notEmpty().withMessage('merchant_name diperlukan')
    .isLength({ max: 100 }).withMessage('merchant_name max 100 karakter'),
  body('fcm_token')
    .notEmpty().withMessage('fcm_token diperlukan')
]

const updateTokenValidation = [
  body('merchant_id').notEmpty().withMessage('merchant_id diperlukan'),
  body('fcm_token').notEmpty().withMessage('fcm_token diperlukan')
]

/**
 * POST /api/merchant/register
 */
const registerMerchant = async (req, res) => {
  try {
    // Validate input
    const errors = validationResult(req)
    if (!errors.isEmpty()) {
      return errorResponse(res, errors.array()[0].msg, 400, 'VALIDATION_ERROR')
    }

    const { merchant_id, merchant_name, fcm_token, phone_number } = req.body

    // Check if merchant already exists
    const existing = await pool.query(
      'SELECT merchant_id, api_key FROM merchants WHERE merchant_id = $1',
      [merchant_id]
    )

    if (existing.rows.length > 0) {
      // Update FCM token if merchant already registered
      await pool.query(
        'UPDATE merchants SET fcm_token = $1, updated_at = NOW() WHERE merchant_id = $2',
        [fcm_token, merchant_id]
      )

      return successResponse(res, {
        merchant_id,
        api_key: existing.rows[0].api_key,
        qris_static: existing.rows[0].qris_static || '',
        message: 'Merchant already registered, FCM token updated'
      })
    }

    // Generate API key
    const apiKey = generateApiKey()

    // Generate static QRIS placeholder
    // In production, this would be a real QRIS code from your payment provider
    const qrisStatic = `STATIC-QRIS-${merchant_id}`

    // Insert merchant
    await pool.query(
      `INSERT INTO merchants
       (merchant_id, merchant_name, fcm_token, api_key, qris_static, phone_number)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [merchant_id, merchant_name, fcm_token, apiKey, qrisStatic, phone_number || null]
    )

    console.log(`✅ Merchant registered: ${merchant_id}`)

    return successResponse(res, {
      merchant_id,
      api_key: apiKey,
      qris_static: qrisStatic
    }, 'Merchant registered successfully', 201)

  } catch (error) {
    console.error('Register merchant error:', error)
    return errorResponse(res, 'Gagal mendaftar merchant', 500)
  }
}

/**
 * PUT /api/merchant/fcm-token
 */
const updateFCMToken = async (req, res) => {
  try {
    const errors = validationResult(req)
    if (!errors.isEmpty()) {
      return errorResponse(res, errors.array()[0].msg, 400)
    }

    const { merchant_id, fcm_token } = req.body

    await pool.query(
      'UPDATE merchants SET fcm_token = $1, updated_at = NOW() WHERE merchant_id = $2',
      [fcm_token, merchant_id]
    )

    console.log(`✅ FCM token updated for merchant: ${merchant_id}`)
    return successResponse(res, null, 'FCM token updated')

  } catch (error) {
    console.error('Update FCM token error:', error)
    return errorResponse(res, 'Gagal update FCM token', 500)
  }
}

/**
 * GET /api/merchant/:merchantId/transactions
 */
const getTransactions = async (req, res) => {
  try {
    const { merchantId } = req.params
    const { date, limit = 100 } = req.query

    let query = `
      SELECT * FROM transactions
      WHERE merchant_id = $1
    `
    const params = [merchantId]

    // Filter by date
    if (date) {
      query += ` AND DATE(created_at) = $2`
      params.push(date)
    } else {
      query += ` AND DATE(created_at) = CURRENT_DATE`
    }

    query += ` ORDER BY created_at DESC LIMIT $${params.length + 1}`
    params.push(parseInt(limit))

    const result = await pool.query(query, params)

    // Get daily total
    const totalResult = await pool.query(
      `SELECT
        COALESCE(SUM(amount), 0) as total_amount,
        COUNT(*) as total_count
       FROM transactions
       WHERE merchant_id = $1
       AND status = 'success'
       AND DATE(created_at) = CURRENT_DATE`,
      [merchantId]
    )

    return successResponse(res, {
      transactions: result.rows,
      total_amount: parseInt(totalResult.rows[0].total_amount),
      total_count: parseInt(totalResult.rows[0].total_count)
    })

  } catch (error) {
    console.error('Get transactions error:', error)
    return errorResponse(res, 'Gagal mengambil transaksi', 500)
  }
}

module.exports = {
  registerMerchant,
  updateFCMToken,
  getTransactions,
  registerValidation,
  updateTokenValidation
}