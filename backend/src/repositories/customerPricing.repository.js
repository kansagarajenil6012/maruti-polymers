const db = require('../config/db');
const { AppError } = require('../utils/response');

class CustomerPricingRepository {
  async getPricesByCustomerId(customerId) {
    const { rows } = await db.query(
      `SELECT cpp.id, cpp.customer_id, cpp.product_id, cpp.selling_price,
              p.product_name, p.size, p.colour, p.default_sell_price
       FROM customer_product_prices cpp
       JOIN products p ON cpp.product_id = p.id
       WHERE cpp.customer_id = $1`,
      [customerId]
    );
    
    // Reshape to match old supabase format
    return rows.map(r => ({
      id: r.id,
      customer_id: r.customer_id,
      product_id: r.product_id,
      selling_price: r.selling_price,
      products: {
        product_name: r.product_name,
        size: r.size,
        colour: r.colour,
        default_sell_price: r.default_sell_price
      }
    }));
  }

  async upsertPrices(customerId, pricesList) {
    const results = [];
    for (const item of pricesList) {
      const { rows } = await db.query(
        `INSERT INTO customer_product_prices (customer_id, product_id, selling_price)
         VALUES ($1, $2, $3)
         ON CONFLICT (customer_id, product_id) DO UPDATE SET selling_price = $3
         RETURNING *`,
        [customerId, item.product_id, item.selling_price]
      );
      results.push(rows[0]);
    }
    return results;
  }
}

module.exports = new CustomerPricingRepository();
