const app = require('./app');
const config = require('./config/environment');

const startServer = () => {
  try {
    app.listen(config.port, () => {
      console.log(`Server is running in ${config.env} mode on port ${config.port}`);
      console.log(`Health check available at http://localhost:${config.port}/api/health`);
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
};

startServer();
