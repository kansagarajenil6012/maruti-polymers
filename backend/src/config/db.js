const { Pool } = require('pg');
const dns = require('dns');
const config = require('./environment');

if (dns.setDefaultResultOrder) {
  dns.setDefaultResultOrder('ipv4first');
}

let connStr = process.env.DATABASE_URL || defaultDbUrl;
if (connStr.includes('db.midxibdobhkjkrkeiwtm.supabase.co')) {
  connStr = connStr
    .replace('db.midxibdobhkjkrkeiwtm.supabase.co:5432', 'aws-0-ap-south-1.pooler.supabase.com:6543')
    .replace('postgresql://postgres:', 'postgresql://postgres.midxibdobhkjkrkeiwtm:');
}

const pool = new Pool({
  connectionString: connStr,
  ssl: {
    rejectUnauthorized: false
  }
});

module.exports = {
  query: (text, params) => pool.query(text, params),
  getClient: () => pool.connect()
};
