const express = require('express');
const router = express.Router();
const invoiceController = require('../controllers/invoice.controller');
const validateRequest = require('../middleware/validateRequest');
const { invoiceSchema, cancelInvoiceSchema, updateInvoiceSchema } = require('../validators/invoice.validator');

router.route('/')
  .get(invoiceController.getAllInvoices)
  .post(validateRequest(invoiceSchema), invoiceController.createInvoice);

router.route('/:id')
  .get(invoiceController.getInvoiceById)
  .put(validateRequest(updateInvoiceSchema), invoiceController.updateInvoice);

router.route('/:id/cancel')
  .post(validateRequest(cancelInvoiceSchema), invoiceController.cancelInvoice);

module.exports = router;
