const { Pool } = require('pg')

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.NODE_ENV === 'production'
    ? { rejectUnauthorized: false }
    : false
})

// Test connection
pool.connect((err, client, release) => {
  if (err) {
    console.error('❌ Database connection error:', err.message)
    return
  }
  console.log('✅ Database connected successfully')
  release()
})

// Initialize tables
const initDatabase = async () => {
  const client = await pool.connect()

  try {
    await client.query('BEGIN')

    // Merchants table
    await client.query(`
      CREATE TABLE IF NOT EXISTS merchants (
        merchant_id VARCHAR(50) PRIMARY KEY,
        merchant_name VARCHAR(100) NOT NULL,
        fcm_token TEXT,
        api_key VARCHAR(100) UNIQUE NOT NULL,
        qris_static TEXT,
        phone_number VARCHAR(20),
        is_active BOOLEAN DEFAULT true,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `)

    // Transactions table
    await client.query(`
      CREATE TABLE IF NOT EXISTS transactions (
        transaction_id VARCHAR(100) PRIMARY KEY,
        merchant_id VARCHAR(50) REFERENCES merchants(merchant_id),
        order_id VARCHAR(150),
        amount INTEGER NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'pending',
        payment_method VARCHAR(50) DEFAULT 'qris',
        customer_name VARCHAR(100),
        paid_at TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        raw_data JSONB
      )
    `)

    // QRIS transactions table
    await client.query(`
      CREATE TABLE IF NOT EXISTS qris_transactions (
        qris_id VARCHAR(100) PRIMARY KEY,
        merchant_id VARCHAR(50) REFERENCES merchants(merchant_id),
        order_id VARCHAR(150) UNIQUE NOT NULL,
        amount INTEGER NOT NULL,
        qris_string TEXT,
        qris_url TEXT,
        status VARCHAR(20) DEFAULT 'pending',
        expires_at TIMESTAMP NOT NULL,
        paid_at TIMESTAMP,
        transaction_id VARCHAR(100),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `)

    // Webhook logs table
    await client.query(`
      CREATE TABLE IF NOT EXISTS webhook_logs (
        id SERIAL PRIMARY KEY,
        order_id VARCHAR(150),
        merchant_id VARCHAR(50),
        payload JSONB NOT NULL,
        signature VARCHAR(500),
        is_valid BOOLEAN DEFAULT false,
        processed BOOLEAN DEFAULT false,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `)

    // Indexes
    await client.query(`
      CREATE INDEX IF NOT EXISTS idx_transactions_merchant
      ON transactions(merchant_id)
    `)
    await client.query(`
      CREATE INDEX IF NOT EXISTS idx_transactions_created
      ON transactions(created_at)
    `)
    await client.query(`
      CREATE INDEX IF NOT EXISTS idx_qris_merchant
      ON qris_transactions(merchant_id)
    `)
    await client.query(`
      CREATE INDEX IF NOT EXISTS idx_qris_order
      ON qris_transactions(order_id)
    `)
    await client.query(`
      CREATE INDEX IF NOT EXISTS idx_qris_status
      ON qris_transactions(status)
    `)

    await client.query('COMMIT')
    console.log('✅ Database tables initialized')

  } catch (error) {
    await client.query('ROLLBACK')
    console.error('❌ Database init error:', error.message)
    throw error
  } finally {
    client.release()
  }
}

module.exports = { pool, initDatabase }