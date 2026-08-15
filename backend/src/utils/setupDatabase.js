require('dotenv').config();
const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

const runSetup = async () => {
    if (!process.env.DATABASE_URL) {
        console.error('DATABASE_URL is missing in .env');
        process.exit(1);
    }

    const client = new Client({
        connectionString: process.env.DATABASE_URL,
        ssl: {
            rejectUnauthorized: false
        }
    });

    try {
        await client.connect();
        console.log('Connected to PostgreSQL successfully.');

        const schemaPath = path.join(__dirname, 'schema.sql');
        const schemaSql = fs.readFileSync(schemaPath, 'utf8');

        console.log('Executing schema.sql...');
        await client.query(schemaSql);
        
        console.log('Database tables and seed data created successfully!');
    } catch (error) {
        console.error('Error setting up database:', error);
    } finally {
        await client.end();
        console.log('Database connection closed.');
    }
};

runSetup();
