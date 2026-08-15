const Joi = require('joi');

const productSchema = Joi.object({
  product_name: Joi.string().max(100).required(),
  size: Joi.string().max(10).required(),
  colour: Joi.string().max(50).required(),
  default_buy_price: Joi.number().min(0).precision(2).required(),
  default_sell_price: Joi.number().min(0).precision(2).required(),
  is_active: Joi.boolean().default(true)
});

const statusSchema = Joi.object({
  is_active: Joi.boolean().required()
});

module.exports = {
  productSchema,
  statusSchema
};
