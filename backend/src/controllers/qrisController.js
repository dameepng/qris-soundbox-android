const { pool } = require('../config/database')
const { generateQRISCoreApi } = require('../services/midtransService')
const { generateOrderId, generateQrisId, successResponse, errorResponse } = require('../utils/helpers')
const { QRIS } = require('../utils/constants')
const { body, validationResult } = require('express-validator')

const generateValidation = [
  body('amount')
    .isInt({ min: QRIS.MIN_AMOUNT, max: QRIS.MAX_AMOUNT })
    .withMessage(`Amount harus antara ${QRIS.MIN_AMOUNT} - ${QRIS.MAX_AMOUNT}`),
  body('merchant_id')
    .notEmpty().withMessage('merchant_id diperlukan')
]

/**
 * POST /api/qris/generate
 */
const generateQRIS = async (req, res) => {
  try {
    const errors = validationResult(req)
    if (!errors.isEmpty()) {
      return errorResponse(res, errors.array()[0].msg, 400, 'VALIDATION_ERROR')
    }

    const { merchant_id, amount, description } = req.body

    // Verify merchant exists
    const merchantResult = await pool.query(
      'SELECT * FROM merchants WHERE merchant_id = $1 AND is_active = true',
      [merchant_id]
    )

    if (merchantResult.rows.length === 0) {
      return errorResponse(res, 'Merchant tidak ditemukan', 404, 'MERCHANT_NOT_FOUND')
    }

    // Generate IDs
    const qrisId = generateQrisId()
    const orderId = generateOrderId(merchant_id)
    const expiresAt = new Date(Date.now() + QRIS.EXPIRY_MINUTES * 60 * 1000)

    console.log(`📝 Generating QRIS: ${orderId} for amount: ${amount}`)

    // Generate QRIS via Midtrans Core API
    const midtransResult = await generateQRISCoreApi({
      orderId,
      amount,
      merchantId: merchant_id,
      description
    })

    if (!midtransResult.success) {
      console.error('❌ Midtrans failed:', midtransResult.error)

      return errorResponse(
        res,
        `Midtrans error: ${midtransResult.error}`,
        500,
        'MIDTRANS_ERROR'
      )
    }

    // Save to database with real Midtrans data
    await pool.query(
      `INSERT INTO qris_transactions
       (qris_id, merchant_id, order_id, amount, qris_string, qris_url, status, expires_at)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
      [
        qrisId,
        merchant_id,
        orderId,
        amount,
        midtransResult.qrisString,
        midtransResult.qrisUrl || midtransResult.qrisString,
        'pending',
        expiresAt
      ]
    )

    console.log(`✅ QRIS generated: ${orderId}`)

    return successResponse(res, {
      qris_id: qrisId,
      order_id: orderId,
      amount: amount,
      qris_string: midtransResult.qrisString,
      qris_url: midtransResult.qrisUrl,
      expires_at: expiresAt.toISOString(),
      status: 'pending',
      is_mock: false // Real Midtrans QRIS
    })

  } catch (error) {
    console.error('Generate QRIS error:', error)
    return errorResponse(res, 'Gagal generate QRIS', 500)
  }
}

/**
 * GET /api/qris/status/:orderId
 */
const checkQRISStatus = async (req, res) => {
  try {
    const { orderId } = req.params

    const result = await pool.query(
      'SELECT * FROM qris_transactions WHERE order_id = $1',
      [orderId]
    )

    if (result.rows.length === 0) {
      return errorResponse(res, 'QRIS tidak ditemukan', 404)
    }

    const qris = result.rows[0]

    // Check if expired
    if (qris.status === 'pending' && new Date(qris.expires_at) < new Date()) {
      await pool.query(
        'UPDATE qris_transactions SET status = $1 WHERE order_id = $2',
        ['expired', orderId]
      )
      qris.status = 'expired'
    }

    return successResponse(res, {
      order_id: qris.order_id,
      status: qris.status,
      amount: qris.amount,
      created_at: qris.created_at,
      expires_at: qris.expires_at,
      paid_at: qris.paid_at
    })

  } catch (error) {
    console.error('Check QRIS status error:', error)
    return errorResponse(res, 'Gagal check status', 500)
  }
}

/**
 * POST /api/qris/cancel
 */
const cancelQRIS = async (req, res) => {
  try {
    const { order_id } = req.body

    await pool.query(
      `UPDATE qris_transactions SET status = 'cancelled'
       WHERE order_id = $1 AND status = 'pending'`,
      [order_id]
    )

    return successResponse(res, null, 'QRIS cancelled')

  } catch (error) {
    console.error('Cancel QRIS error:', error)
    return errorResponse(res, 'Gagal cancel QRIS', 500)
  }
}

module.exports = {
  generateQRIS,
  checkQRISStatus,
  cancelQRIS,
  generateValidation
}