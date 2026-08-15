const db = require('../config/db');
const { AppError } = require('../utils/response');

class CustomerRepository {
  async findAll(filters = {}) {
    let sql = 'SELECT * FROM customers WHERE 1=1';
    const params = [];

    if (filters.is_active !== undefined) {
      params.push(filters.is_active);
      sql += ` AND is_active = $${params.length}`;
    }

    if (filters.search) {
      params.push(`%${filters.search}%`);
      sql += ` AND (customer_name ILIKE $${params.length} OR mobile ILIKE $${params.length} OR customer_code ILIKE $${params.length})`;
    }

    sql += ' ORDER BY customer_name ASC';

    const { rows } = await db.query(sql, params);
    return rows;
  }

  async findById(id) {
    const { rows } = await db.query('SELECT * FROM customers WHERE id = $1', [id]);
    return rows[0] || null;
  }

  async generateCustomerCode() {
    const { rows } = await db.query(
      "SELECT customer_code FROM customers ORDER BY customer_code DESC LIMIT 1"
    );

    let nextNumber = 1;
    if (rows.length > 0 && rows[0].customer_code) {
      const lastNumber = parseInt(rows[0].customer_code.split('-')[1], 10);
      if (!isNaN(lastNumber)) {
        nextNumber = lastNumber + 1;
      }
    }

    return `CUS-${String(nextNumber).padStart(6, '0')}`;
  }

  async create(customerData) {
    const cols = Object.keys(customerData);
    const vals = Object.values(customerData);
    const placeholders = cols.map((_, i) => `$${i + 1}`);

    try {
      const { rows } = await db.query(
        `INSERT INTO customers (${cols.join(',')}) VALUES (${placeholders.join(',')}) RETURNING *`,
        vals
      );
      return rows[0];
    } catch (error) {
      if (error.code === '23505') throw new AppError('Customer code already exists', 400);
      throw new AppError(error.message, 500);
    }
  }

  async update(id, customerData) {
    const cols = Object.keys(customerData);
    const vals = Object.values(customerData);
    const setClause = cols.map((col, i) => `${col} = $${i + 1}`).join(', ');
    vals.push(id);

    try {
      const { rows } = await db.query(
        `UPDATE customers SET ${setClause}, updated_at = NOW() WHERE id = $${vals.length} RETURNING *`,
        vals
      );
      return rows[0];
    } catch (error) {
      throw new AppError(error.message, 500);
    }
  }
}

module.exports = new CustomerRepository();
