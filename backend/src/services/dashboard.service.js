const db = require('../config/db');
const { AppError } = require('../utils/response');

class DashboardService {
  async getSummary() {
    try {
      const today = new Date().toISOString().split('T')[0];
      const monthStart = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0];

      // Total Customers & Products
      const customerRes = await db.query('SELECT COUNT(*) FROM customers WHERE is_active = true');
      const productRes = await db.query('SELECT COUNT(*) FROM products WHERE is_active = true');

      // Today's Sales
      const todaySalesRes = await db.query(
        'SELECT COALESCE(SUM(total_amount), 0) as total FROM invoices WHERE invoice_date = $1 AND status != $2',
        [today, 'CANCELLED']
      );

      // Current Month Sales
      const monthSalesRes = await db.query(
        'SELECT COALESCE(SUM(total_amount), 0) as total FROM invoices WHERE invoice_date >= $1 AND status != $2',
        [monthStart, 'CANCELLED']
      );

      // Today's Collection
      const todayCollectionRes = await db.query(
        'SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE payment_date = $1',
        [today]
      );

      // Pending Invoices
      const pendingRes = await db.query(
        "SELECT COUNT(*) FROM invoices WHERE status IN ('PENDING', 'PARTIAL')"
      );

      // Total Outstanding (Opening balances + Invoices - Payments)
      const outstandingRes = await db.query(`
        WITH inv AS (
          SELECT COALESCE(SUM(total_amount), 0) as amt FROM invoices WHERE status != 'CANCELLED'
        ),
        pay AS (
          SELECT COALESCE(SUM(amount), 0) as amt FROM payments
        ),
        op AS (
          SELECT COALESCE(SUM(opening_balance), 0) as amt FROM customers
        )
        SELECT (op.amt + inv.amt - pay.amt) as total_outstanding
        FROM inv, pay, op
      `);

      // Top Outstanding Customers
      const topCustomersRes = await db.query(`
        SELECT 
          c.id, c.customer_name,
          COALESCE(c.opening_balance, 0) + COALESCE(SUM(i.total_amount), 0) - COALESCE((SELECT SUM(amount) FROM payments WHERE customer_id = c.id), 0) as outstanding
        FROM customers c
        LEFT JOIN invoices i ON c.id = i.customer_id AND i.status != 'CANCELLED'
        GROUP BY c.id, c.customer_name, c.opening_balance
        HAVING COALESCE(c.opening_balance, 0) + COALESCE(SUM(i.total_amount), 0) - COALESCE((SELECT SUM(amount) FROM payments WHERE customer_id = c.id), 0) > 0
        ORDER BY outstanding DESC
        LIMIT 5
      `);

      // Recent Invoices
      const recentInvoicesRes = await db.query(
        `SELECT i.id, i.invoice_no, i.total_amount, i.status, c.customer_name
         FROM invoices i
         JOIN customers c ON i.customer_id = c.id
         ORDER BY i.created_at DESC
         LIMIT 5`
      );

      return {
        today_sales: parseFloat(todaySalesRes.rows[0].total),
        month_sales: parseFloat(monthSalesRes.rows[0].total),
        today_collection: parseFloat(todayCollectionRes.rows[0].total),
        total_outstanding: parseFloat(outstandingRes.rows[0].total_outstanding),
        pending_invoices_count: parseInt(pendingRes.rows[0].count),
        total_customers: parseInt(customerRes.rows[0].count),
        total_products: parseInt(productRes.rows[0].count),
        top_outstanding_customers: topCustomersRes.rows.map(r => ({ ...r, outstanding: parseFloat(r.outstanding) })),
        recent_invoices: recentInvoicesRes.rows
      };

    } catch (error) {
      throw new AppError('Error fetching dashboard summary: ' + error.message, 500);
    }
  }
}

module.exports = new DashboardService();
