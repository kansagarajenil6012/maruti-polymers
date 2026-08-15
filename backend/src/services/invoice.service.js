const invoiceRepository = require('../repositories/invoice.repository');
const customerRepository = require('../repositories/customer.repository');
const productRepository = require('../repositories/product.repository');
const customerPricingRepository = require('../repositories/customerPricing.repository');
const { getFinancialYear } = require('../utils/financialYear');
const { AppError } = require('../utils/response');

class InvoiceService {
  async getAllInvoices(filters) {
    return await invoiceRepository.findAll(filters);
  }

  async getInvoiceById(id) {
    const invoice = await invoiceRepository.findById(id);
    if (!invoice) throw new AppError('Invoice not found', 404);
    return invoice;
  }

  async createInvoice(data) {
    // 1. Validate customer
    const customer = await customerRepository.findById(data.customer_id);
    if (!customer || !customer.is_active) {
      throw new AppError('Customer not found or is inactive', 400);
    }

    // 2. Fetch custom prices for this customer
    const customPrices = await customerPricingRepository.getPricesByCustomerId(data.customer_id);
    const priceMap = {};
    customPrices.forEach(cp => {
      priceMap[cp.product_id] = Number(cp.selling_price);
    });

    // 3. Process each item
    let subtotal = 0;
    const itemsData = [];

    for (const item of data.items) {
      const product = await productRepository.findById(item.product_id);
      if (!product || !product.is_active) {
        throw new AppError(`Product ${item.product_id} not found or inactive`, 400);
      }

      // Resolve Rate
      const rate = priceMap[product.id] !== undefined ? priceMap[product.id] : Number(product.default_sell_price);
      
      const qty = Number(item.qty);
      const discount = Number(item.discount || 0);
      const amount = (qty * rate) - discount;

      if (amount < 0) {
        throw new AppError(`Item discount cannot exceed item total for ${product.product_name}`, 400);
      }

      subtotal += amount;

      itemsData.push({
        product_id: product.id,
        product_name: product.product_name,
        qty,
        rate,
        discount,
        amount
      });
    }

    // 4. Calculate invoice totals
    const invoiceDiscount = Number(data.discount || 0);
    const totalAmount = subtotal - invoiceDiscount;

    if (totalAmount < 0) {
      throw new AppError('Invoice discount cannot exceed subtotal', 400);
    }

    const paidAmount = Number(data.paid_amount || 0);
    if (paidAmount > totalAmount) {
      throw new AppError('Paid amount cannot exceed total invoice amount', 400);
    }

    const pendingAmount = totalAmount - paidAmount;
    
    let status = 'PENDING';
    if (paidAmount === totalAmount) status = 'PAID';
    else if (paidAmount > 0) status = 'PARTIAL';

    const invoiceDate = data.invoice_date ? new Date(data.invoice_date) : new Date();
    const financialYear = getFinancialYear(invoiceDate);

    const invoiceData = {
      customer_id: customer.id,
      invoice_date: invoiceDate.toISOString().split('T')[0],
      financial_year: financialYear,
      subtotal,
      discount: invoiceDiscount,
      total_amount: totalAmount,
      paid_amount: paidAmount,
      pending_amount: pendingAmount,
      status,
      remarks: data.remarks
    };

    let paymentData = null;
    if (paidAmount > 0) {
      paymentData = {
        amount: paidAmount,
        payment_mode: data.payment_mode,
        reference_no: data.payment_reference
      };
    }

    // 5. Execute transaction
    return await invoiceRepository.createTransactional(invoiceData, itemsData, paymentData);
  }

  async cancelInvoice(id, cancelReason) {
    const existing = await invoiceRepository.findById(id);
    if (!existing) throw new AppError('Invoice not found', 404);
    if (existing.status === 'CANCELLED') throw new AppError('Invoice is already cancelled', 400);

    // Cancel the invoice (note: does not delete associated payments, they remain as general credits)
    return await invoiceRepository.cancelInvoice(id, cancelReason);
  }

  async updateInvoice(id, data) {
    const existing = await invoiceRepository.findById(id);
    if (!existing) throw new AppError('Invoice not found', 404);
    if (existing.status === 'CANCELLED') throw new AppError('Cannot update cancelled invoice', 400);

    // Fetch custom prices for the customer
    const customPrices = await customerPricingRepository.getPricesByCustomerId(existing.customer_id);
    const priceMap = {};
    customPrices.forEach(cp => {
      priceMap[cp.product_id] = Number(cp.selling_price);
    });

    let subtotal = 0;
    const itemsData = [];

    for (const item of data.items) {
      const product = await productRepository.findById(item.product_id);
      if (!product || !product.is_active) {
        throw new AppError(`Product ${item.product_id} not found or inactive`, 400);
      }

      const rate = priceMap[product.id] !== undefined ? priceMap[product.id] : Number(product.default_sell_price);
      const qty = Number(item.qty);
      const discount = Number(item.discount || 0);
      const amount = (qty * rate) - discount;

      if (amount < 0) {
        throw new AppError(`Item discount cannot exceed item total for ${product.product_name}`, 400);
      }

      subtotal += amount;

      itemsData.push({
        product_id: product.id,
        product_name: product.product_name,
        qty,
        rate,
        discount,
        amount
      });
    }

    const invoiceDiscount = Number(data.discount !== undefined ? data.discount : existing.discount);
    const totalAmount = subtotal - invoiceDiscount;

    if (totalAmount < 0) {
      throw new AppError('Invoice discount cannot exceed subtotal', 400);
    }

    const paidAmount = Number(existing.paid_amount);
    if (totalAmount < paidAmount) {
      throw new AppError(`New total (${totalAmount}) cannot be less than already paid amount (${paidAmount})`, 400);
    }

    const pendingAmount = totalAmount - paidAmount;
    
    let status = 'PENDING';
    if (paidAmount === totalAmount) status = 'PAID';
    else if (paidAmount > 0) status = 'PARTIAL';

    const updatedInvoiceData = {
      subtotal,
      discount: invoiceDiscount,
      total_amount: totalAmount,
      pending_amount: pendingAmount,
      status,
      remarks: data.remarks !== undefined ? data.remarks : existing.remarks
    };

    return await invoiceRepository.updateTransactional(id, updatedInvoiceData, itemsData);
  }
}

module.exports = new InvoiceService();
