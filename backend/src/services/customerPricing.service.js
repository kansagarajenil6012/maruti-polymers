const customerPricingRepository = require('../repositories/customerPricing.repository');
const customerRepository = require('../repositories/customer.repository');
const { AppError } = require('../utils/response');

class CustomerPricingService {
  async getCustomerPrices(customerId) {
    // Validate customer exists
    const customer = await customerRepository.findById(customerId);
    if (!customer) throw new AppError('Customer not found', 404);

    return await customerPricingRepository.getPricesByCustomerId(customerId);
  }

  async upsertCustomerPrices(customerId, pricesList) {
    // Validate customer exists
    const customer = await customerRepository.findById(customerId);
    if (!customer) throw new AppError('Customer not found', 404);

    return await customerPricingRepository.upsertPrices(customerId, pricesList);
  }
}

module.exports = new CustomerPricingService();
