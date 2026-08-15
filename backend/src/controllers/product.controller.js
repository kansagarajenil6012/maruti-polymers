const productService = require('../services/product.service');
const { sendResponse } = require('../utils/response');

exports.getAllProducts = async (req, res, next) => {
  try {
    const filters = {
      size: req.query.size,
      colour: req.query.colour,
      search: req.query.search,
      is_active: req.query.is_active !== undefined ? req.query.is_active === 'true' : undefined
    };
    
    const products = await productService.getAllProducts(filters);
    sendResponse(res, 200, products, 'Products retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.getProductById = async (req, res, next) => {
  try {
    const product = await productService.getProductById(req.params.id);
    sendResponse(res, 200, product, 'Product retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.createProduct = async (req, res, next) => {
  try {
    const product = await productService.createProduct(req.body);
    sendResponse(res, 201, product, 'Product created successfully');
  } catch (error) {
    next(error);
  }
};

exports.updateProduct = async (req, res, next) => {
  try {
    const product = await productService.updateProduct(req.params.id, req.body);
    sendResponse(res, 200, product, 'Product updated successfully');
  } catch (error) {
    next(error);
  }
};

exports.updateProductStatus = async (req, res, next) => {
  try {
    const product = await productService.updateProductStatus(req.params.id, req.body.is_active);
    sendResponse(res, 200, product, `Product ${req.body.is_active ? 'activated' : 'deactivated'} successfully`);
  } catch (error) {
    next(error);
  }
};
