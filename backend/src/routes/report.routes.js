const express = require('express');
const router = express.Router();
const reportController = require('../controllers/report.controller');

router.get('/sales', reportController.getSalesRegister);
router.get('/payments', reportController.getPaymentRegister);
router.get('/product-sales', reportController.getProductSales);

module.exports = router;
