const db = require('../config/db');
const customerRepository = require('../repositories/customer.repository');
const { AppError } = require('../utils/response');

class LedgerService {
  async getOutstanding(customerId) {
    const customer = await customerRepository.findById(customerId);
    if (!customer) throw new AppError('Customer not found', 404);

    let outstanding = Number(customer.opening_balance || 0);

    // Sum of all active invoices
    const invRes = await db.query(
      "SELECT COALESCE(SUM(total_amount), 0) as total FROM invoices WHERE customer_id = $1 AND status != 'CANCELLED'",
      [customerId]
    );
    const totalInvoiced = parseFloat(invRes.rows[0].total);

    // Sum of all payments
    const payRes = await db.query(
      'SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE customer_id = $1',
      [customerId]
    );
    const totalPaid = parseFloat(payRes.rows[0].total);

    outstanding = outstanding + totalInvoiced - totalPaid;

    return {
      customer_id: customerId,
      customer_name: customer.customer_name,
      opening_balance: customer.opening_balance,
      total_invoiced: totalInvoiced,
      total_paid: totalPaid,
      outstanding_amount: outstanding
    };
  }

  async getLedger(customerId, fromDate, toDate) {
    const customer = await customerRepository.findById(customerId);
    if (!customer) throw new AppError('Customer not found', 404);

    // Fetch Invoices
    let invSql = "SELECT id, invoice_no, invoice_date, total_amount, status, created_at FROM invoices WHERE customer_id = $1 AND status != 'CANCELLED'";
    const invParams = [customerId];
    
    if (fromDate && toDate) {
      invParams.push(fromDate, toDate);
      invSql += ` AND invoice_date >= $${invParams.length - 1} AND invoice_date <= $${invParams.length}`;
    }

    const invRes = await db.query(invSql, invParams);

    // Fetch Payments
    let paySql = 'SELECT id, payment_date, amount, payment_mode, reference_no, created_at FROM payments WHERE customer_id = $1';
    const payParams = [customerId];
    
    if (fromDate && toDate) {
      payParams.push(fromDate, toDate);
      paySql += ` AND payment_date >= $${payParams.length - 1} AND payment_date <= $${payParams.length}`;
    }

    const payRes = await db.query(paySql, payParams);

    // Combine and Sort
    const entries = [];

    entries.push({
      date: 'N/A',
      created_at: new Date(0).toISOString(),
      particulars: 'Opening Balance',
      debit: Number(customer.opening_balance || 0) > 0 ? Number(customer.opening_balance) : 0,
      credit: Number(customer.opening_balance || 0) < 0 ? Math.abs(Number(customer.opening_balance)) : 0,
      type: 'OPENING'
    });

    invRes.rows.forEach(inv => {
      entries.push({
        id: inv.id,
        date: inv.invoice_date,
        created_at: inv.created_at,
        particulars: `Invoice ${inv.invoice_no}`,
        debit: Number(inv.total_amount),
        credit: 0,
        type: 'INVOICE'
      });
    });

    payRes.rows.forEach(pay => {
      entries.push({
        id: pay.id,
        date: pay.payment_date,
        created_at: pay.created_at,
        particulars: `Payment Received (${pay.payment_mode})`,
        debit: 0,
        credit: Number(pay.amount),
        type: 'PAYMENT'
      });
    });

    // Sort chronologically
    entries.sort((a, b) => new Date(a.created_at) - new Date(b.created_at));

    // Calculate Running Balance
    let currentBalance = 0;
    entries.forEach(entry => {
      currentBalance = currentBalance + entry.debit - entry.credit;
      entry.balance = currentBalance;
    });

    return entries;
  }
}

module.exports = new LedgerService();
