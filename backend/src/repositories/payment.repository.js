const db = require('../config/db');
const { AppError } = require('../utils/response');

class PaymentRepository {
  async findAll(filters = {}) {
    let sql = `SELECT p.*, c.customer_name, c.customer_code 
               FROM payments p 
               JOIN customers c ON p.customer_id = c.id 
               WHERE 1=1`;
    const params = [];

    if (filters.customer_id) {
      params.push(filters.customer_id);
      sql += ` AND p.customer_id = $${params.length}`;
    }
    if (filters.payment_mode) {
      params.push(filters.payment_mode);
      sql += ` AND p.payment_mode = $${params.length}`;
    }
    if (filters.from_date && filters.to_date) {
      params.push(filters.from_date, filters.to_date);
      sql += ` AND p.payment_date >= $${params.length - 1} AND p.payment_date <= $${params.length}`;
    }

    sql += ' ORDER BY p.created_at DESC';

    const { rows } = await db.query(sql, params);
    return rows;
  }

  async createTransactional(paymentData, invoiceToUpdate = null) {
    const client = await db.getClient();
    
    try {
      await client.query('BEGIN');

      const paymentRes = await client.query(
        `INSERT INTO payments 
         (customer_id, invoice_id, payment_date, amount, payment_mode, reference_no, remarks) 
         VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
        [
          paymentData.customer_id, paymentData.invoice_id, paymentData.payment_date,
          paymentData.amount, paymentData.payment_mode, paymentData.reference_no,
          paymentData.remarks
        ]
      );
      const newPayment = paymentRes.rows[0];

      if (invoiceToUpdate) {
        await client.query(
          `UPDATE invoices 
           SET paid_amount = $1, pending_amount = $2, status = $3, updated_at = NOW() 
           WHERE id = $4`,
          [
            invoiceToUpdate.new_paid_amount,
            invoiceToUpdate.new_pending_amount,
            invoiceToUpdate.new_status,
            invoiceToUpdate.id
          ]
        );
      }

      await client.query('COMMIT');
      return newPayment;
    } catch (e) {
      await client.query('ROLLBACK');
      throw new AppError('Payment transaction failed: ' + e.message, 500);
    } finally {
      client.release();
    }
  }
}

module.exports = new PaymentRepository();
