const reportService = require('../services/report.service');
const { sendResponse } = require('../utils/response');

exports.getSalesRegister = async (req, res, next) => {
  try {
    const filters = {
      from_date: req.query.from_date,
      to_date: req.query.to_date,
      customer_id: req.query.customer_id,
      status: req.query.status
    };
    const report = await reportService.getSalesRegister(filters);
    sendResponse(res, 200, report, 'Sales register generated successfully');
  } catch (error) {
    next(error);
  }
};

exports.getPaymentRegister = async (req, res, next) => {
  try {
    const filters = {
      from_date: req.query.from_date,
      to_date: req.query.to_date,
      customer_id: req.query.customer_id,
      payment_mode: req.query.payment_mode
    };
    const report = await reportService.getPaymentRegister(filters);
    sendResponse(res, 200, report, 'Payment register generated successfully');
  } catch (error) {
    next(error);
  }
};

exports.getProductSales = async (req, res, next) => {
  try {
    const filters = {
      from_date: req.query.from_date,
      to_date: req.query.to_date,
      product_id: req.query.product_id
    };
    const report = await reportService.getProductSales(filters);
    sendResponse(res, 200, report, 'Product sales generated successfully');
  } catch (error) {
    next(error);
  }
};
