const customerService = require('../services/customer.service');
const { sendResponse } = require('../utils/response');

exports.getAllCustomers = async (req, res, next) => {
  try {
    const filters = {
      search: req.query.search,
      is_active: req.query.is_active !== undefined ? req.query.is_active === 'true' : undefined
    };
    
    const customers = await customerService.getAllCustomers(filters);
    sendResponse(res, 200, customers, 'Customers retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.getCustomerById = async (req, res, next) => {
  try {
    const customer = await customerService.getCustomerById(req.params.id);
    sendResponse(res, 200, customer, 'Customer retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.createCustomer = async (req, res, next) => {
  try {
    const customer = await customerService.createCustomer(req.body);
    sendResponse(res, 201, customer, 'Customer created successfully');
  } catch (error) {
    next(error);
  }
};

exports.updateCustomer = async (req, res, next) => {
  try {
    const customer = await customerService.updateCustomer(req.params.id, req.body);
    sendResponse(res, 200, customer, 'Customer updated successfully');
  } catch (error) {
    next(error);
  }
};

exports.updateCustomerStatus = async (req, res, next) => {
  try {
    const customer = await customerService.updateCustomerStatus(req.params.id, req.body.is_active);
    sendResponse(res, 200, customer, `Customer ${req.body.is_active ? 'activated' : 'deactivated'} successfully`);
  } catch (error) {
    next(error);
  }
};
