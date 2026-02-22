const crypto = require('crypto')

const { v4: uuidv4 } = require('uuid')

// Generate unique order ID (MAX 50 chars for Midtrans!)
const generateOrderId = (merchantId) => {
  // Use shorter format: ORDER-TIMESTAMP-RANDOM
  const timestamp = Date.now().toString().slice(-8) // Last 8 digits
  const random = Math.random().toString(36).substring(2, 7).toUpperCase() // 5 chars

  // Format: ORDER-12345678-ABCDE (max 23 chars)
  return `ORDER-${timestamp}-${random}`
}

// Alternative with merchant prefix (if needed, max 50 chars)
const generateOrderIdWithMerchant = (merchantId) => {
  const timestamp = Date.now().toString().slice(-8)
  const random = Math.random().toString(36).substring(2, 6).toUpperCase()

  // Take first 10 chars of merchant ID
  const shortMerchantId = merchantId.substring(0, 10).toUpperCase()

  // Format: MID-ORDER-12345678-ABCD (max 28 chars)
  return `${shortMerchantId}-${timestamp}-${random}`
}

// Generate unique QRIS ID
const generateQrisId = () => {
  return `QRIS-${Date.now()}-${uuidv4().substring(0, 8).toUpperCase()}`
}

// Generate API key for merchant
const generateApiKey = () => {
  return `SK-${uuidv4().replace(/-/g, '').toUpperCase()}`
}

// Format rupiah
const formatRupiah = (amount) => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(amount)
}

// Get start of day timestamp
const getStartOfDay = () => {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return now.toISOString()
}

// Success response
const successResponse = (res, data, message = 'Success', statusCode = 200) => {
  return res.status(statusCode).json({
    success: true,
    message,
    data
  })
}

// Error response
const errorResponse = (res, message = 'Internal server error', statusCode = 500, code = null) => {
  return res.status(statusCode).json({
    success: false,
    error: message,
    code
  })
}

module.exports = {
  generateOrderId,
  generateOrderIdWithMerchant,
  generateQrisId,
  generateApiKey,
  formatRupiah,
  getStartOfDay,
  successResponse,
  errorResponse
}