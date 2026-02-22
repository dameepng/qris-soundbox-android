const express = require('express')
const router = express.Router()
const { handleMidtransWebhook, handleTestWebhook } = require('../controllers/webhookController')
const { validateMidtransWebhook } = require('../middleware/validateWebhook')

// Midtrans webhook (validates signature)
router.post('/payment', validateMidtransWebhook, handleMidtransWebhook)

// Test endpoint (development only)
router.post('/test', handleTestWebhook)

module.exports = router