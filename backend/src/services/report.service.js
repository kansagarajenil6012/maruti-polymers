const db = require('../config/db');
const { AppError } = require('../utils/response');

class ReportService {
  async getSalesRegister(filters) {
    try {
      let query = `
        SELECT i.invoice_no, i.invoice_date, c.customer_name, i.subtotal, i.discount, i.total_amount, i.paid_amount, i.status 
        FROM invoices i 
        JOIN customers c ON i.customer_id = c.id
        WHERE 1=1
      `;
      const params = [];
      let paramCount = 1;

      if (filters.from_date && filters.to_date) {
        query += ` AND i.invoice_date >= $${paramCount++} AND i.invoice_date <= $${paramCount++}`;
        params.push(filters.from_date, filters.to_date);
      }
      if (filters.customer_id) {
        query += ` AND i.customer_id = $${paramCount++}`;
        params.push(filters.customer_id);
      }
      if (filters.status) {
        query += ` AND i.status = $${paramCount++}`;
        params.push(filters.status);
      }

      query += ` ORDER BY i.invoice_date DESC, i.created_at DESC`;

      const result = await db.query(query, params);
      return result.rows;
    } catch (error) {
      throw new AppError('Error generating sales register: ' + error.message, 500);
    }
  }

  async getPaymentRegister(filters) {
    try {
      let query = `
        SELECT p.payment_date, c.customer_name, p.amount, p.payment_mode, p.reference_no, p.remarks
        FROM payments p
        JOIN customers c ON p.customer_id = c.id
        WHERE 1=1
      `;
      const params = [];
      let paramCount = 1;

      if (filters.from_date && filters.to_date) {
        query += ` AND p.payment_date >= $${paramCount++} AND p.payment_date <= $${paramCount++}`;
        params.push(filters.from_date, filters.to_date);
      }
      if (filters.customer_id) {
        query += ` AND p.customer_id = $${paramCount++}`;
        params.push(filters.customer_id);
      }
      if (filters.payment_mode) {
        query += ` AND p.payment_mode = $${paramCount++}`;
        params.push(filters.payment_mode);
      }

      query += ` ORDER BY p.payment_date DESC, p.created_at DESC`;

      const result = await db.query(query, params);
      return result.rows;
    } catch (error) {
      throw new AppError('Error generating payment register: ' + error.message, 500);
    }
  }

  async getProductSales(filters) {
    try {
      let query = `
        SELECT ii.product_name, SUM(ii.qty) as total_qty, SUM(ii.amount) as total_amount
        FROM invoice_items ii
        JOIN invoices i ON ii.invoice_id = i.id
        WHERE i.status != 'CANCELLED'
      `;
      const params = [];
      let paramCount = 1;

      if (filters.from_date && filters.to_date) {
        query += ` AND i.invoice_date >= $${paramCount++} AND i.invoice_date <= $${paramCount++}`;
        params.push(filters.from_date, filters.to_date);
      }
      if (filters.product_id) {
        query += ` AND ii.product_id = $${paramCount++}`;
        params.push(filters.product_id);
      }

      query += ` GROUP BY ii.product_name ORDER BY total_amount DESC`;

      const result = await db.query(query, params);
      return result.rows.map(row => ({
        product_name: row.product_name,
        total_qty: parseFloat(row.total_qty),
        total_amount: parseFloat(row.total_amount)
      }));
    } catch (error) {
      throw new AppError('Error generating product sales report: ' + error.message, 500);
    }
  }
}

module.exports = new ReportService();
