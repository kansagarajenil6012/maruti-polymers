const supabase = require('../config/supabase');
const { AppError } = require('../utils/response');

class ProductRepository {
  async findAll(filters = {}) {
    let query = supabase.from('products').select('*');

    if (filters.size) query = query.eq('size', filters.size);
    if (filters.colour) query = query.eq('colour', filters.colour);
    if (filters.is_active !== undefined) query = query.eq('is_active', filters.is_active);
    
    if (filters.search) {
      query = query.ilike('product_name', `%${filters.search}%`);
    }

    query = query.order('size', { ascending: true }).order('colour', { ascending: true });

    const { data, error } = await query;
    if (error) throw new AppError(error.message, 500);
    return data;
  }

  async findById(id) {
    const { data, error } = await supabase
      .from('products')
      .select('*')
      .eq('id', id)
      .single();

    if (error && error.code !== 'PGRST116') throw new AppError(error.message, 500);
    return data;
  }

  async create(productData) {
    const { data, error } = await supabase
      .from('products')
      .insert([productData])
      .select()
      .single();

    if (error) {
      if (error.code === '23505') throw new AppError('Product with this size and colour already exists', 400);
      throw new AppError(error.message, 500);
    }
    return data;
  }

  async update(id, productData) {
    const { data, error } = await supabase
      .from('products')
      .update(productData)
      .eq('id', id)
      .select()
      .single();

    if (error) {
      if (error.code === '23505') throw new AppError('Product with this size and colour already exists', 400);
      throw new AppError(error.message, 500);
    }
    return data;
  }
}

module.exports = new ProductRepository();
