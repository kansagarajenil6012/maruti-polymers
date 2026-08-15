const { Pool } = require('pg');
const dns = require('dns');
const config = require('./environment');

if (dns.setDefaultResultOrder) {
  dns.setDefaultResultOrder('ipv4first');
}

const defaultDbUrl = 'postgresql://postgres.midxibdobhkjkrkeiwtm:KEje8S8QmlCps1il@aws-0-ap-south-1.pooler.supabase.com:6543/postgres';

const pool = new Pool({
  connectionString: process.env.DATABASE_URL || defaultDbUrl,
  ssl: {
    rejectUnauthorized: false
  }
});

module.exports = {
  query: (text, params) => pool.query(text, params),
  getClient: () => pool.connect()
};
