const express = require('express');
const router = express.Router();
const paymentController = require('../controllers/payment.controller');
const validateRequest = require('../middleware/validateRequest');
const { paymentSchema } = require('../validators/payment.validator');

router.route('/')
  .get(paymentController.getAllPayments)
  .post(validateRequest(paymentSchema), paymentController.receivePayment);

module.exports = router;
