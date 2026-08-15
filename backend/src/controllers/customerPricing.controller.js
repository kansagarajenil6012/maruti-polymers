const customerPricingService = require('../services/customerPricing.service');
const { sendResponse } = require('../utils/response');

exports.getCustomerPrices = async (req, res, next) => {
  try {
    const prices = await customerPricingService.getCustomerPrices(req.params.id);
    sendResponse(res, 200, prices, 'Customer prices retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.upsertCustomerPrices = async (req, res, next) => {
  try {
    const prices = await customerPricingService.upsertCustomerPrices(req.params.id, req.body.prices);
    sendResponse(res, 200, prices, 'Customer prices updated successfully');
  } catch (error) {
    next(error);
  }
};
