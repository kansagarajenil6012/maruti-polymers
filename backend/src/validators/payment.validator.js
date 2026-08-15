const Joi = require('joi');

const paymentSchema = Joi.object({
  customer_id: Joi.string().uuid().required(),
  invoice_id: Joi.string().uuid().allow(null, ''),
  payment_date: Joi.date().iso().default(() => new Date()),
  amount: Joi.number().min(0.01).precision(2).required(),
  payment_mode: Joi.string().valid('CASH', 'BANK', 'UPI', 'CHEQUE', 'OTHER').required(),
  reference_no: Joi.string().allow(null, ''),
  remarks: Joi.string().allow(null, '')
});

module.exports = {
  paymentSchema
};
