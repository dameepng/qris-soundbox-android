const { pool } = require('../config/database')
const { errorResponse } = require('../utils/helpers')

/**
 * Validate API Key from header X-API-Key
 */
const authenticateMerchant = async (req, res, next) => {
  try {
    const apiKey = req.headers['x-api-key']

    if (!apiKey) {
      return errorResponse(res, 'API Key diperlukan', 401, 'MISSING_API_KEY')
    }

    // Check API key in database
    const result = await pool.query(
      'SELECT * FROM merchants WHERE api_key = $1 AND is_active = true',
      [apiKey]
    )

    if (result.rows.length === 0) {
      return errorResponse(res, 'API Key tidak valid', 401, 'INVALID_API_KEY')
    }

    // Attach merchant to request
    req.merchant = result.rows[0]
    next()

  } catch (error) {
    console.error('Auth middleware error:', error)
    return errorResponse(res, 'Authentication error', 500)
  }
}

module.exports = { authenticateMerchant }