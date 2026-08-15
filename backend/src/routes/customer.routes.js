const express = require('express');
const router = express.Router();
const customerController = require('../controllers/customer.controller');
const customerPricingController = require('../controllers/customerPricing.controller');
const ledgerController = require('../controllers/ledger.controller');
const validateRequest = require('../middleware/validateRequest');
const { customerSchema, statusSchema } = require('../validators/customer.validator');
const { pricingSchema } = require('../validators/pricing.validator');

router.route('/')
  .get(customerController.getAllCustomers)
  .post(validateRequest(customerSchema), customerController.createCustomer);

router.route('/:id')
  .get(customerController.getCustomerById)
  .put(validateRequest(customerSchema), customerController.updateCustomer);

router.route('/:id/status')
  .patch(validateRequest(statusSchema), customerController.updateCustomerStatus);

router.route('/:id/prices')
  .get(customerPricingController.getCustomerPrices)
  .put(validateRequest(pricingSchema), customerPricingController.upsertCustomerPrices);

router.route('/:id/outstanding')
  .get(ledgerController.getOutstanding);

router.route('/:id/ledger')
  .get(ledgerController.getLedger);

module.exports = router;
