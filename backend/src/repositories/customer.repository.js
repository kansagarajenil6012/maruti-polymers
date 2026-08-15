const supabase = require('../config/supabase');
const { AppError } = require('../utils/response');

class CustomerRepository {
  async findAll(filters = {}) {
    let query = supabase.from('customers').select('*');

    if (filters.is_active !== undefined) query = query.eq('is_active', filters.is_active);
    
    if (filters.search) {
      query = query.or(`customer_name.ilike.%${filters.search}%,mobile.ilike.%${filters.search}%,customer_code.ilike.%${filters.search}%`);
    }

    query = query.order('customer_name', { ascending: true });

    const { data, error } = await query;
    if (error) throw new AppError(error.message, 500);
    return data;
  }

  async findById(id) {
    const { data, error } = await supabase
      .from('customers')
      .select('*')
      .eq('id', id)
      .single();

    if (error && error.code !== 'PGRST116') throw new AppError(error.message, 500);
    return data;
  }

  async generateCustomerCode() {
    // Basic implementation for low concurrency
    // For high concurrency, use a PostgreSQL function with advisory lock
    const { data, error } = await supabase
      .from('customers')
      .select('customer_code')
      .order('customer_code', { ascending: false })
      .limit(1);

    if (error) throw new AppError(error.message, 500);

    let nextNumber = 1;
    if (data && data.length > 0 && data[0].customer_code) {
      const lastCode = data[0].customer_code;
      const lastNumber = parseInt(lastCode.split('-')[1], 10);
      if (!isNaN(lastNumber)) {
        nextNumber = lastNumber + 1;
      }
    }

    return `CUS-${String(nextNumber).padStart(6, '0')}`;
  }

  async create(customerData) {
    const { data, error } = await supabase
      .from('customers')
      .insert([customerData])
      .select()
      .single();

    if (error) {
      if (error.code === '23505') throw new AppError('Customer code already exists', 400);
      throw new AppError(error.message, 500);
    }
    return data;
  }

  async update(id, customerData) {
    const { data, error } = await supabase
      .from('customers')
      .update(customerData)
      .eq('id', id)
      .select()
      .single();

    if (error) {
      throw new AppError(error.message, 500);
    }
    return data;
  }
}

module.exports = new CustomerRepository();
