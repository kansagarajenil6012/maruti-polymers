const invoiceService = require('../services/invoice.service');
const { sendResponse } = require('../utils/response');

exports.getAllInvoices = async (req, res, next) => {
  try {
    const filters = {
      customer_id: req.query.customer_id,
      status: req.query.status,
      from_date: req.query.from_date,
      to_date: req.query.to_date
    };
    
    const invoices = await invoiceService.getAllInvoices(filters);
    sendResponse(res, 200, invoices, 'Invoices retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.getInvoiceById = async (req, res, next) => {
  try {
    const invoice = await invoiceService.getInvoiceById(req.params.id);
    sendResponse(res, 200, invoice, 'Invoice retrieved successfully');
  } catch (error) {
    next(error);
  }
};

exports.createInvoice = async (req, res, next) => {
  try {
    const invoice = await invoiceService.createInvoice(req.body);
    sendResponse(res, 201, invoice, 'Invoice created successfully');
  } catch (error) {
    next(error);
  }
};

exports.cancelInvoice = async (req, res, next) => {
  try {
    const invoice = await invoiceService.cancelInvoice(req.params.id, req.body.cancel_reason);
    sendResponse(res, 200, invoice, 'Invoice cancelled successfully');
  } catch (error) {
    next(error);
  }
};

exports.updateInvoice = async (req, res, next) => {
  try {
    const invoice = await invoiceService.updateInvoice(req.params.id, req.body);
    sendResponse(res, 200, invoice, 'Invoice updated successfully');
  } catch (error) {
    next(error);
  }
};
