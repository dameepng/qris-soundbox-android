require('dotenv').config()

const express = require('express')
const helmet = require('helmet')
const cors = require('cors')
const morgan = require('morgan')

const { initDatabase } = require('./src/config/database')
const { initFirebase } = require('./src/config/firebase')

const merchantRoutes = require('./src/routes/merchant')
const qrisRoutes = require('./src/routes/qris')
const webhookRoutes = require('./src/routes/webhook')

const app = express()
const PORT = process.env.PORT || 3000

// ─── Middleware ───────────────────────────────────────────────

app.use(helmet())
app.use(cors())
app.use(morgan('combined'))

// Parse JSON - must be before routes
app.use(express.json())
app.use(express.urlencoded({ extended: true }))

// ─── Routes ───────────────────────────────────────────────────

app.use('/api/merchant', merchantRoutes)
app.use('/api/qris', qrisRoutes)
app.use('/webhook', webhookRoutes)

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV,
    version: '1.0.0'
  })
})

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    success: false,
    error: `Route ${req.originalUrl} not found`
  })
})

// Error handler
app.use((err, req, res, next) => {
  console.error('Unhandled error:', err)
  res.status(500).json({
    success: false,
    error: 'Internal server error'
  })
})

// ─── Start Server ─────────────────────────────────────────────

const startServer = async () => {
  try {
    // Initialize Firebase
    initFirebase()

    // Initialize Database
    await initDatabase()

    // Start listening
    app.listen(PORT, () => {
      console.log(`
╔══════════════════════════════════════╗
║   🔊 Soundbox QRIS Backend           ║
║   Running on port: ${PORT}              ║
║   Environment: ${process.env.NODE_ENV}       ║
╚══════════════════════════════════════╝
      `)
    })

  } catch (error) {
    console.error('❌ Failed to start server:', error)
    process.exit(1)
  }
}

startServer()

module.exports = app