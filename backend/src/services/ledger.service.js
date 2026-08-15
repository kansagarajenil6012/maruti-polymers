const supabase = require('../config/supabase');
const customerRepository = require('../repositories/customer.repository');
const { AppError } = require('../utils/response');

class LedgerService {
  async getOutstanding(customerId) {
    const customer = await customerRepository.findById(customerId);
    if (!customer) throw new AppError('Customer not found', 404);

    let outstanding = Number(customer.opening_balance || 0);

    // Sum of all active invoices
    const { data: invoices, error: invError } = await supabase
      .from('invoices')
      .select('total_amount')
      .eq('customer_id', customerId)
      .neq('status', 'CANCELLED');

    if (invError) throw new AppError(invError.message, 500);

    const totalInvoiced = invoices.reduce((sum, inv) => sum + Number(inv.total_amount), 0);
    
    // Sum of all payments
    const { data: payments, error: payError } = await supabase
      .from('payments')
      .select('amount')
      .eq('customer_id', customerId);

    if (payError) throw new AppError(payError.message, 500);

    const totalPaid = payments.reduce((sum, pay) => sum + Number(pay.amount), 0);

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
    let invQuery = supabase.from('invoices')
      .select('id, invoice_no, invoice_date, total_amount, status, created_at')
      .eq('customer_id', customerId)
      .neq('status', 'CANCELLED');
    
    if (fromDate && toDate) {
      invQuery = invQuery.gte('invoice_date', fromDate).lte('invoice_date', toDate);
    }
    
    const { data: invoices, error: invError } = await invQuery;
    if (invError) throw new AppError(invError.message, 500);

    // Fetch Payments
    let payQuery = supabase.from('payments')
      .select('id, payment_date, amount, payment_mode, reference_no, created_at')
      .eq('customer_id', customerId);
    
    if (fromDate && toDate) {
      payQuery = payQuery.gte('payment_date', fromDate).lte('payment_date', toDate);
    }

    const { data: payments, error: payError } = await payQuery;
    if (payError) throw new AppError(payError.message, 500);

    // Combine and Sort
    const entries = [];

    // Push opening balance if no date filter or from_date is earlier
    // For MVP, we'll just put it at the top as an initial entry.
    entries.push({
      date: 'N/A',
      created_at: new Date(0).toISOString(), // ensure it sorts first
      particulars: 'Opening Balance',
      debit: Number(customer.opening_balance || 0) > 0 ? Number(customer.opening_balance) : 0,
      credit: Number(customer.opening_balance || 0) < 0 ? Math.abs(Number(customer.opening_balance)) : 0,
      type: 'OPENING'
    });

    invoices.forEach(inv => {
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

    payments.forEach(pay => {
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
