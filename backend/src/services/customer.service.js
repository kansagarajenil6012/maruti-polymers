const customerRepository = require('../repositories/customer.repository');
const { AppError } = require('../utils/response');

class CustomerService {
  async getAllCustomers(filters) {
    return await customerRepository.findAll(filters);
  }

  async getCustomerById(id) {
    const customer = await customerRepository.findById(id);
    if (!customer) {
      throw new AppError('Customer not found', 404);
    }
    // Note: Outstanding calculation will be appended later when Ledger is implemented
    return customer;
  }

  async createCustomer(customerData) {
    const customerCode = await customerRepository.generateCustomerCode();
    customerData.customer_code = customerCode;
    return await customerRepository.create(customerData);
  }

  async updateCustomer(id, customerData) {
    const existing = await customerRepository.findById(id);
    if (!existing) {
      throw new AppError('Customer not found', 404);
    }
    return await customerRepository.update(id, customerData);
  }

  async updateCustomerStatus(id, isActive) {
    const existing = await customerRepository.findById(id);
    if (!existing) {
      throw new AppError('Customer not found', 404);
    }
    return await customerRepository.update(id, { is_active: isActive });
  }
}

module.exports = new CustomerService();
