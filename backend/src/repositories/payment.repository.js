const supabase = require('../config/supabase');
const db = require('../config/db');
const { AppError } = require('../utils/response');

class PaymentRepository {
  async findAll(filters = {}) {
    let query = supabase.from('payments').select('*, customers(customer_name, customer_code)');

    if (filters.customer_id) query = query.eq('customer_id', filters.customer_id);
    if (filters.payment_mode) query = query.eq('payment_mode', filters.payment_mode);
    
    if (filters.from_date && filters.to_date) {
      query = query.gte('payment_date', filters.from_date).lte('payment_date', filters.to_date);
    }

    query = query.order('created_at', { ascending: false });

    const { data, error } = await query;
    if (error) throw new AppError(error.message, 500);
    return data;
  }

  async createTransactional(paymentData, invoiceToUpdate = null) {
    const client = await db.getClient();
    
    try {
      await client.query('BEGIN');

      // 1. Insert Payment
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

      // 2. Update Invoice (if linked)
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
