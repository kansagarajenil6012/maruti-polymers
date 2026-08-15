const supabase = require('../config/supabase');
const { AppError } = require('../utils/response');

class CustomerPricingRepository {
  async getPricesByCustomerId(customerId) {
    const { data, error } = await supabase
      .from('customer_product_prices')
      .select(`
        id,
        customer_id,
        product_id,
        selling_price,
        products (
          product_name,
          size,
          colour,
          default_sell_price
        )
      `)
      .eq('customer_id', customerId);

    if (error) throw new AppError(error.message, 500);
    return data;
  }

  async upsertPrices(customerId, pricesList) {
    // prepare the data for upsert
    const upsertData = pricesList.map(item => ({
      customer_id: customerId,
      product_id: item.product_id,
      selling_price: item.selling_price
    }));

    const { data, error } = await supabase
      .from('customer_product_prices')
      .upsert(upsertData, { onConflict: 'customer_id,product_id' })
      .select();

    if (error) throw new AppError(error.message, 500);
    return data;
  }
}

module.exports = new CustomerPricingRepository();
