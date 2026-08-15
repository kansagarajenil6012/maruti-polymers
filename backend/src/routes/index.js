const express = require('express');
const router = express.Router();

const productRoutes = require('./product.routes');
const customerRoutes = require('./customer.routes');
const invoiceRoutes = require('./invoice.routes');
const paymentRoutes = require('./payment.routes');
const dashboardRoutes = require('./dashboard.routes');
const reportRoutes = require('./report.routes');

router.use('/products', productRoutes);
router.use('/customers', customerRoutes);
router.use('/invoices', invoiceRoutes);
router.use('/payments', paymentRoutes);
router.use('/dashboard', dashboardRoutes);
router.use('/reports', reportRoutes);

module.exports = router;
