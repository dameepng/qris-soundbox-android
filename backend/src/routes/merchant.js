const express = require('express')
const router = express.Router()
const {
  registerMerchant,
  updateFCMToken,
  getTransactions,
  registerValidation,
  updateTokenValidation
} = require('../controllers/merchantController')
const { authenticateMerchant } = require('../middleware/auth')

// Public routes
router.post('/register', registerValidation, registerMerchant)

// Protected routes (need API key)
router.put('/fcm-token', authenticateMerchant, updateTokenValidation, updateFCMToken)
router.get('/:merchantId/transactions', authenticateMerchant, getTransactions)

module.exports = router