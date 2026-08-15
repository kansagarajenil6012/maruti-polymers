require('dotenv').config();
const { Pool } = require('pg');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }
});

async function migrate() {
  try {
    await pool.query('ALTER TABLE invoice_items ADD COLUMN IF NOT EXISTS buy_rate NUMERIC(10,2) NOT NULL DEFAULT 0');
    console.log('Migration successful: Added buy_rate to invoice_items');
  } catch (err) {
    console.error('Migration failed:', err);
  } finally {
    pool.end();
  }
}

migrate();
