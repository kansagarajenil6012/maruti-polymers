const express = require('express');
const router = express.Router();
const productController = require('../controllers/product.controller');
const validateRequest = require('../middleware/validateRequest');
const { productSchema, statusSchema } = require('../validators/product.validator');

router.route('/')
  .get(productController.getAllProducts)
  .post(validateRequest(productSchema), productController.createProduct);

router.route('/:id')
  .get(productController.getProductById)
  .put(validateRequest(productSchema), productController.updateProduct);

router.route('/:id/status')
  .patch(validateRequest(statusSchema), productController.updateProductStatus);

module.exports = router;
