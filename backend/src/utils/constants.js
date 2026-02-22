module.exports = {
  // Transaction status
  STATUS: {
    PENDING: 'pending',
    SUCCESS: 'success',
    FAILED: 'failed',
    EXPIRED: 'expired',
    CANCELLED: 'cancelled'
  },

  // Midtrans transaction status
  MIDTRANS_STATUS: {
    CAPTURE: 'capture',
    SETTLEMENT: 'settlement',
    PENDING: 'pending',
    DENY: 'deny',
    CANCEL: 'cancel',
    EXPIRE: 'expire',
    FAILURE: 'failure'
  },

  // QRIS config
  QRIS: {
    EXPIRY_MINUTES: 5,
    MIN_AMOUNT: 1000,
    MAX_AMOUNT: 10000000
  },

  // FCM
  FCM: {
    TYPE_PAYMENT: 'payment'
  }
}