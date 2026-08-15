const Joi = require('joi');

const customerSchema = Joi.object({
  customer_name: Joi.string().max(150).required(),
  mobile: Joi.string().max(15).allow(null, ''),
  email: Joi.string().email().max(100).allow(null, ''),
  address: Joi.string().allow(null, ''),
  city: Joi.string().max(50).allow(null, ''),
  state: Joi.string().max(50).allow(null, ''),
  pincode: Joi.string().max(10).allow(null, ''),
  opening_balance: Joi.number().precision(2).default(0),
  is_active: Joi.boolean().default(true)
});

const statusSchema = Joi.object({
  is_active: Joi.boolean().required()
});

module.exports = {
  customerSchema,
  statusSchema
};
