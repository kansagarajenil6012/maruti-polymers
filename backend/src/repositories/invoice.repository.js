const supabase = require('../config/supabase');
const db = require('../config/db');
const { AppError } = require('../utils/response');

class InvoiceRepository {
  async findAll(filters = {}) {
    let query = supabase.from('invoices').select('*, customers(customer_name, customer_code)');

    if (filters.customer_id) query = query.eq('customer_id', filters.customer_id);
    if (filters.status) query = query.eq('status', filters.status);
    
    if (filters.from_date && filters.to_date) {
      query = query.gte('invoice_date', filters.from_date).lte('invoice_date', filters.to_date);
    }

    query = query.order('created_at', { ascending: false });

    const { data, error } = await query;
    if (error) throw new AppError(error.message, 500);
    return data;
  }

  async findById(id) {
    const { data, error } = await supabase
      .from('invoices')
      .select(`
        *,
        customers(customer_name, mobile, address, city, state, pincode),
        invoice_items(
          id, product_id, product_name, qty, rate, discount, amount
        ),
        payments(
          id, payment_date, amount, payment_mode, reference_no
        )
      `)
      .eq('id', id)
      .single();

    if (error && error.code !== 'PGRST116') throw new AppError(error.message, 500);
    return data;
  }

  async createTransactional(invoiceData, itemsData, paymentData) {
    const client = await db.getClient();
    
    try {
      await client.query('BEGIN');

      // 1. Generate Invoice Number with advisory lock / select for update
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

      // 2. Insert Invoice
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

      // 3. Insert Invoice Items
      for (const item of itemsData) {
        await client.query(
          `INSERT INTO invoice_items 
           (invoice_id, product_id, product_name, qty, rate, discount, amount) 
           VALUES ($1, $2, $3, $4, $5, $6, $7)`,
          [
            newInvoice.id, item.product_id, item.product_name,
            item.qty, item.rate, item.discount, item.amount
          ]
        );
      }

      // 4. Insert Initial Payment if paid_amount > 0
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
    const { data, error } = await supabase
      .from('invoices')
      .update({ status: 'CANCELLED', cancel_reason: cancelReason, updated_at: new Date() })
      .eq('id', id)
      .eq('status', 'PENDING') // Only allow cancelling pending invoices, or maybe others too. Let's not restrict here.
      .select()
      .single();

    if (error) throw new AppError(error.message, 500);
    return data;
  }

  async updateTransactional(invoiceId, updatedInvoiceData, newItemsData) {
    const client = await db.getClient();
    
    try {
      await client.query('BEGIN');

      // 1. Delete old items
      await client.query('DELETE FROM invoice_items WHERE invoice_id = $1', [invoiceId]);

      // 2. Update Invoice
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

      // 3. Insert New Items
      for (const item of newItemsData) {
        await client.query(
          `INSERT INTO invoice_items 
           (invoice_id, product_id, product_name, qty, rate, discount, amount) 
           VALUES ($1, $2, $3, $4, $5, $6, $7)`,
          [
            invoiceId, item.product_id, item.product_name,
            item.qty, item.rate, item.discount, item.amount
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
