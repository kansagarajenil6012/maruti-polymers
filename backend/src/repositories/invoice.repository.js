const db = require('../config/db');
const { AppError } = require('../utils/response');

class InvoiceRepository {
  async findAll(filters = {}) {
    let sql = `SELECT i.*, c.customer_name, c.customer_code 
               FROM invoices i 
               JOIN customers c ON i.customer_id = c.id 
               WHERE 1=1`;
    const params = [];

    if (filters.customer_id) {
      params.push(filters.customer_id);
      sql += ` AND i.customer_id = $${params.length}`;
    }
    if (filters.status) {
      params.push(filters.status);
      sql += ` AND i.status = $${params.length}`;
    }
    if (filters.from_date && filters.to_date) {
      params.push(filters.from_date, filters.to_date);
      sql += ` AND i.invoice_date >= $${params.length - 1} AND i.invoice_date <= $${params.length}`;
    }

    sql += ' ORDER BY i.created_at DESC';

    const { rows } = await db.query(sql, params);
    return rows;
  }

  async findById(id) {
    // Get invoice with customer info
    const invoiceRes = await db.query(
      `SELECT i.*, c.customer_name, c.mobile, c.address, c.city, c.state, c.pincode
       FROM invoices i
       JOIN customers c ON i.customer_id = c.id
       WHERE i.id = $1`,
      [id]
    );

    if (invoiceRes.rows.length === 0) return null;

    const invoice = invoiceRes.rows[0];

    // Get items
    const itemsRes = await db.query(
      'SELECT id, product_id, product_name, qty, rate, discount, amount FROM invoice_items WHERE invoice_id = $1',
      [id]
    );
    invoice.invoice_items = itemsRes.rows;

    // Get payments
    const paymentsRes = await db.query(
      'SELECT id, payment_date, amount, payment_mode, reference_no FROM payments WHERE invoice_id = $1',
      [id]
    );
    invoice.payments = paymentsRes.rows;

    // Restructure customer info to match previous format
    invoice.customers = {
      customer_name: invoice.customer_name,
      mobile: invoice.mobile,
      address: invoice.address,
      city: invoice.city,
      state: invoice.state,
      pincode: invoice.pincode
    };

    return invoice;
  }

  async createTransactional(invoiceData, itemsData, paymentData) {
    const client = await db.getClient();
    
    try {
      await client.query('BEGIN');

      const seqRes = await client.query(
        'SELECT last_number FROM invoice_number_sequences WHERE financial_year = $1 FOR UPDATE',
        [invoiceData.financial_year]
      );
      
      let lastNumber = 0;
      if (seqRes.rows.length === 0) {
        await client.query(
          'INSERT INTO invoice_number_sequences (financial_year, last_number) VALUES ($1, $2)',
          [invoiceData.financial_year, 0]
        );
      } else {
        lastNumber = seqRes.rows[0].last_number;
      }
      
      const nextNumber = lastNumber + 1;
      await client.query(
        'UPDATE invoice_number_sequences SET last_number = $1 WHERE financial_year = $2',
        [nextNumber, invoiceData.financial_year]
      );
      
      const invoiceNo = `INV/${invoiceData.financial_year}/${String(nextNumber).padStart(6, '0')}`;

      const invoiceRes = await client.query(
        `INSERT INTO invoices 
         (invoice_no, customer_id, invoice_date, subtotal, discount, total_amount, paid_amount, pending_amount, status, remarks) 
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNING *`,
        [
          invoiceNo, invoiceData.customer_id, invoiceData.invoice_date, 
          invoiceData.subtotal, invoiceData.discount, invoiceData.total_amount, 
          invoiceData.paid_amount, invoiceData.pending_amount, invoiceData.status, 
          invoiceData.remarks
        ]
      );
      const newInvoice = invoiceRes.rows[0];

      for (const item of itemsData) {
        await client.query(
          `INSERT INTO invoice_items 
           (invoice_id, product_id, product_name, qty, rate, buy_rate, discount, amount) 
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
          [
            newInvoice.id, item.product_id, item.product_name,
            item.qty, item.rate, item.buy_rate, item.discount, item.amount
          ]
        );
      }

      if (paymentData && paymentData.amount > 0) {
        await client.query(
          `INSERT INTO payments 
           (customer_id, invoice_id, payment_date, amount, payment_mode, reference_no, remarks) 
           VALUES ($1, $2, $3, $4, $5, $6, $7)`,
          [
            newInvoice.customer_id, newInvoice.id, invoiceData.invoice_date,
            paymentData.amount, paymentData.payment_mode, paymentData.reference_no,
            'Initial payment for ' + invoiceNo
          ]
        );
      }

      await client.query('COMMIT');
      return newInvoice;
    } catch (e) {
      await client.query('ROLLBACK');
      throw new AppError('Transaction failed: ' + e.message, 500);
    } finally {
      client.release();
    }
  }

  async cancelInvoice(id, cancelReason) {
    const { rows } = await db.query(
      `UPDATE invoices SET status = 'CANCELLED', cancel_reason = $1, updated_at = NOW() WHERE id = $2 RETURNING *`,
      [cancelReason, id]
    );
    if (rows.length === 0) throw new AppError('Invoice not found', 404);
    return rows[0];
  }

  async updateTransactional(invoiceId, updatedInvoiceData, newItemsData) {
    const client = await db.getClient();
    
    try {
      await client.query('BEGIN');

      await client.query('DELETE FROM invoice_items WHERE invoice_id = $1', [invoiceId]);

      const invoiceRes = await client.query(
        `UPDATE invoices 
         SET subtotal = $1, discount = $2, total_amount = $3, pending_amount = $4, status = $5, remarks = $6, updated_at = NOW()
         WHERE id = $7 RETURNING *`,
        [
          updatedInvoiceData.subtotal, updatedInvoiceData.discount, updatedInvoiceData.total_amount,
          updatedInvoiceData.pending_amount, updatedInvoiceData.status, updatedInvoiceData.remarks,
          invoiceId
        ]
      );
      const updatedInvoice = invoiceRes.rows[0];

      for (const item of newItemsData) {
        await client.query(
          `INSERT INTO invoice_items 
           (invoice_id, product_id, product_name, qty, rate, buy_rate, discount, amount) 
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
          [
            invoiceId, item.product_id, item.product_name,
            item.qty, item.rate, item.buy_rate, item.discount, item.amount
          ]
        );
      }

      await client.query('COMMIT');
      return updatedInvoice;
    } catch (e) {
      await client.query('ROLLBACK');
      throw new AppError('Transaction failed: ' + e.message, 500);
    } finally {
      client.release();
    }
  }
}

module.exports = new InvoiceRepository();
