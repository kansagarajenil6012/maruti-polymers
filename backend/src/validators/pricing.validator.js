const Joi = require('joi');

const pricingSchema = Joi.object({
  prices: Joi.array().items(
    Joi.object({
      product_id: Joi.string().uuid().required(),
      selling_price: Joi.number().min(0).precision(2).required()
    })
  ).min(1).required()
});

module.exports = {
  pricingSchema
};
