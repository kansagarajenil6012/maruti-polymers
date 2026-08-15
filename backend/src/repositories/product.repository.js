const db = require('../config/db');
const { AppError } = require('../utils/response');

class ProductRepository {
  async findAll(filters = {}) {
    let sql = 'SELECT * FROM products WHERE 1=1';
    const params = [];

    if (filters.size) {
      params.push(filters.size);
      sql += ` AND size = $${params.length}`;
    }
    if (filters.colour) {
      params.push(filters.colour);
      sql += ` AND colour = $${params.length}`;
    }
    if (filters.is_active !== undefined) {
      params.push(filters.is_active);
      sql += ` AND is_active = $${params.length}`;
    }
    if (filters.search) {
      params.push(`%${filters.search}%`);
      sql += ` AND product_name ILIKE $${params.length}`;
    }

    sql += ' ORDER BY size ASC, colour ASC';

    const { rows } = await db.query(sql, params);
    return rows;
  }

  async findById(id) {
    const { rows } = await db.query('SELECT * FROM products WHERE id = $1', [id]);
    return rows[0] || null;
  }

  async create(productData) {
    const cols = Object.keys(productData);
    const vals = Object.values(productData);
    const placeholders = cols.map((_, i) => `$${i + 1}`);

    try {
      const { rows } = await db.query(
        `INSERT INTO products (${cols.join(',')}) VALUES (${placeholders.join(',')}) RETURNING *`,
        vals
      );
      return rows[0];
    } catch (error) {
      if (error.code === '23505') throw new AppError('Product with this size and colour already exists', 400);
      throw new AppError(error.message, 500);
    }
  }

  async update(id, productData) {
    const cols = Object.keys(productData);
    const vals = Object.values(productData);
    const setClause = cols.map((col, i) => `${col} = $${i + 1}`).join(', ');
    vals.push(id);

    try {
      const { rows } = await db.query(
        `UPDATE products SET ${setClause}, updated_at = NOW() WHERE id = $${vals.length} RETURNING *`,
        vals
      );
      return rows[0];
    } catch (error) {
      if (error.code === '23505') throw new AppError('Product with this size and colour already exists', 400);
      throw new AppError(error.message, 500);
    }
  }
}

module.exports = new ProductRepository();
