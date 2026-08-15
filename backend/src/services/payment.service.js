const paymentRepository = require('../repositories/payment.repository');
const customerRepository = require('../repositories/customer.repository');
const invoiceRepository = require('../repositories/invoice.repository');
const { AppError } = require('../utils/response');

class PaymentService {
  async getAllPayments(filters) {
    return await paymentRepository.findAll(filters);
  }

  async receivePayment(data) {
    // 1. Validate customer
    const customer = await customerRepository.findById(data.customer_id);
    if (!customer) throw new AppError('Customer not found', 404);

    let invoiceToUpdate = null;

    // 2. Validate invoice if provided
    if (data.invoice_id) {
      const invoice = await invoiceRepository.findById(data.invoice_id);
      if (!invoice) throw new AppError('Invoice not found', 404);
      if (invoice.customer_id !== data.customer_id) throw new AppError('Invoice does not belong to this customer', 400);
      if (invoice.status === 'CANCELLED') throw new AppError('Cannot receive payment for cancelled invoice', 400);

      const amountToPay = Number(data.amount);
      const pendingAmount = Number(invoice.pending_amount);

      if (amountToPay > pendingAmount) {
        throw new AppError(`Payment amount (${amountToPay}) exceeds invoice pending amount (${pendingAmount})`, 400);
      }

      const newPaidAmount = Number(invoice.paid_amount) + amountToPay;
      const newPendingAmount = Number(invoice.total_amount) - newPaidAmount;
      
      let newStatus = 'PARTIAL';
      if (newPaidAmount === Number(invoice.total_amount)) newStatus = 'PAID';

      invoiceToUpdate = {
        id: invoice.id,
        new_paid_amount: newPaidAmount,
        new_pending_amount: newPendingAmount,
        new_status: newStatus
      };
    }

    const paymentDate = data.payment_date ? new Date(data.payment_date) : new Date();

    const paymentData = {
      customer_id: data.customer_id,
      invoice_id: data.invoice_id || null,
      payment_date: paymentDate.toISOString().split('T')[0],
      amount: data.amount,
      payment_mode: data.payment_mode,
      reference_no: data.reference_no,
      remarks: data.remarks
    };

    return await paymentRepository.createTransactional(paymentData, invoiceToUpdate);
  }
}

module.exports = new PaymentService();
