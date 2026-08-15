const Joi = require('joi');

const invoiceItemSchema = Joi.object({
  product_id: Joi.string().uuid().required(),
  qty: Joi.number().min(0.01).precision(2).required(),
  discount: Joi.number().min(0).precision(2).default(0)
});

const invoiceSchema = Joi.object({
  customer_id: Joi.string().uuid().required(),
  invoice_date: Joi.date().iso().default(() => new Date()),
  discount: Joi.number().min(0).precision(2).default(0),
  paid_amount: Joi.number().min(0).precision(2).default(0),
  remarks: Joi.string().allow(null, ''),
  items: Joi.array().items(invoiceItemSchema).min(1).required(),
  payment_mode: Joi.string().valid('CASH', 'BANK', 'UPI', 'CHEQUE', 'OTHER').when('paid_amount', {
    is: Joi.number().greater(0),
    then: Joi.required(),
    otherwise: Joi.optional()
  }),
  payment_reference: Joi.string().allow(null, '')
});

const cancelInvoiceSchema = Joi.object({
  cancel_reason: Joi.string().min(5).required()
});

const updateInvoiceSchema = Joi.object({
  discount: Joi.number().min(0).precision(2).optional(),
  remarks: Joi.string().allow(null, '').optional(),
  items: Joi.array().items(invoiceItemSchema).min(1).required()
});

module.exports = {
  invoiceSchema,
  cancelInvoiceSchema,
  updateInvoiceSchema
};
