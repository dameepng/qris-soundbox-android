const express = require('express')
const router = express.Router()
const {
  generateQRIS,
  checkQRISStatus,
  cancelQRIS,
  generateValidation
} = require('../controllers/qrisController')
const { authenticateMerchant } = require('../middleware/auth')

// All QRIS routes need authentication
router.post('/generate', authenticateMerchant, generateValidation, generateQRIS)
router.get('/status/:orderId', authenticateMerchant, checkQRISStatus)
router.post('/cancel', authenticateMerchant, cancelQRIS)

module.exports = router