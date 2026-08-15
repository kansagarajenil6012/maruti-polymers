const dashboardService = require('../services/dashboard.service');
const { sendResponse } = require('../utils/response');

exports.getSummary = async (req, res, next) => {
  try {
    const summary = await dashboardService.getSummary();
    sendResponse(res, 200, summary, 'Dashboard summary retrieved successfully');
  } catch (error) {
    next(error);
  }
};
