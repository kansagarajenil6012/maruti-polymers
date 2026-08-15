const paymentService = require('../services/payment.service');
const { sendResponse } = require('../utils/response');

exports.getAllPayments = async (req, res, next) => {
  try {
    const filters = {
      customer_id: req.query.customer_id,
      payment_mode: req.query.payment_mode,
      from_date: req.query.from_date,
      to_date: req.query.to_date
    };
    
    const payments = await paymentService.getAllPayments(filters);
    sendResponse(res, 200, payments, 'Payments retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.receivePayment = async (req, res, next) => {
  try {
    const payment = await paymentService.receivePayment(req.body);
    sendResponse(res, 201, payment, 'Payment received successfully');
  } catch (error) {
    next(error);
  }
};
