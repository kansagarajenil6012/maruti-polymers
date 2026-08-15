const productRepository = require('../repositories/product.repository');
const { AppError } = require('../utils/response');

class ProductService {
  async getAllProducts(filters) {
    return await productRepository.findAll(filters);
  }

  async getProductById(id) {
    const product = await productRepository.findById(id);
    if (!product) {
      throw new AppError('Product not found', 404);
    }
    return product;
  }

  async createProduct(productData) {
    return await productRepository.create(productData);
  }

  async updateProduct(id, productData) {
    const existing = await productRepository.findById(id);
    if (!existing) {
      throw new AppError('Product not found', 404);
    }
    return await productRepository.update(id, productData);
  }

  async updateProductStatus(id, isActive) {
    const existing = await productRepository.findById(id);
    if (!existing) {
      throw new AppError('Product not found', 404);
    }
    return await productRepository.update(id, { is_active: isActive });
  }
}

module.exports = new ProductService();
