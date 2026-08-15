const ledgerService = require('../services/ledger.service');
const { sendResponse } = require('../utils/response');

exports.getOutstanding = async (req, res, next) => {
  try {
    const outstanding = await ledgerService.getOutstanding(req.params.id);
    sendResponse(res, 200, outstanding, 'Outstanding calculated successfully');
  } catch (error) {
    next(error);
  }
};

exports.getLedger = async (req, res, next) => {
  try {
    const ledger = await ledgerService.getLedger(req.params.id, req.query.from_date, req.query.to_date);
    sendResponse(res, 200, ledger, 'Ledger retrieved successfully');
  } catch (error) {
    next(error);
  }
};
