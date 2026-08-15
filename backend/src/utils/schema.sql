-- Maruti Polymer PVC Edge Band Tape DB Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Products Table
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(100) NOT NULL,
    size VARCHAR(10) NOT NULL,
    colour VARCHAR(50) NOT NULL,
    default_buy_price NUMERIC(10,2) NOT NULL DEFAULT 0,
    default_sell_price NUMERIC(10,2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(size, colour)
);

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_code VARCHAR(20) NOT NULL UNIQUE,
    customer_name VARCHAR(150) NOT NULL,
    mobile VARCHAR(15),
    email VARCHAR(100),
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    pincode VARCHAR(10),
    opening_balance NUMERIC(12,2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Customer Product Prices Table
CREATE TABLE IF NOT EXISTS customer_product_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    product_id UUID NOT NULL REFERENCES products(id),
    selling_price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(customer_id, product_id)
);

-- 4. Invoices Table
CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_no VARCHAR(30) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),
    invoice_date DATE NOT NULL DEFAULT CURRENT_DATE,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    pending_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PARTIAL', 'PAID', 'CANCELLED')),
    remarks TEXT,
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (paid_amount <= total_amount),
    CHECK (discount >= 0),
    CHECK (total_amount >= 0)
);

-- 5. Invoice Items Table
CREATE TABLE IF NOT EXISTS invoice_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    product_name VARCHAR(100) NOT NULL,
    qty NUMERIC(10,2) NOT NULL,
    rate NUMERIC(10,2) NOT NULL,
    discount NUMERIC(10,2) NOT NULL DEFAULT 0,
    amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    invoice_id UUID REFERENCES invoices(id),
    payment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    payment_mode VARCHAR(20) NOT NULL CHECK (payment_mode IN ('CASH', 'BANK', 'UPI', 'CHEQUE', 'OTHER')),
    reference_no VARCHAR(100),
    remarks TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. Invoice Number Sequences Table
CREATE TABLE IF NOT EXISTS invoice_number_sequences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    financial_year VARCHAR(10) NOT NULL UNIQUE,
    last_number INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'set_updated_at_products') THEN
        CREATE TRIGGER set_updated_at_products BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'set_updated_at_customers') THEN
        CREATE TRIGGER set_updated_at_customers BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'set_updated_at_customer_prices') THEN
        CREATE TRIGGER set_updated_at_customer_prices BEFORE UPDATE ON customer_product_prices FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'set_updated_at_invoices') THEN
        CREATE TRIGGER set_updated_at_invoices BEFORE UPDATE ON invoices FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'set_updated_at_invoice_seq') THEN
        CREATE TRIGGER set_updated_at_invoice_seq BEFORE UPDATE ON invoice_number_sequences FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_products_size ON products(size);
CREATE INDEX IF NOT EXISTS idx_products_is_active ON products(is_active);
CREATE INDEX IF NOT EXISTS idx_customers_name ON customers(customer_name);
CREATE INDEX IF NOT EXISTS idx_customers_mobile ON customers(mobile);
CREATE INDEX IF NOT EXISTS idx_customers_is_active ON customers(is_active);
CREATE INDEX IF NOT EXISTS idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_date ON invoices(invoice_date);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice_id ON invoice_items(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_customer_id ON payments(customer_id);
CREATE INDEX IF NOT EXISTS idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_date ON payments(payment_date);

-- Seed Initial Products (25 products as per requirement)
-- Sizes: 20MM (7), 40MM (7), 22MM (7), 12MM (2), 13MM (2)

INSERT INTO products (product_name, size, colour, default_buy_price, default_sell_price)
VALUES 
    -- 20MM (7 colours)
    ('20MM-White', '20MM', 'White', 100, 150),
    ('20MM-Black', '20MM', 'Black', 100, 150),
    ('20MM-Red', '20MM', 'Red', 105, 155),
    ('20MM-Blue', '20MM', 'Blue', 105, 155),
    ('20MM-Green', '20MM', 'Green', 105, 155),
    ('20MM-Yellow', '20MM', 'Yellow', 105, 155),
    ('20MM-Brown', '20MM', 'Brown', 105, 155),
    
    -- 40MM (7 colours)
    ('40MM-White', '40MM', 'White', 180, 250),
    ('40MM-Black', '40MM', 'Black', 180, 250),
    ('40MM-Red', '40MM', 'Red', 185, 255),
    ('40MM-Blue', '40MM', 'Blue', 185, 255),
    ('40MM-Green', '40MM', 'Green', 185, 255),
    ('40MM-Yellow', '40MM', 'Yellow', 185, 255),
    ('40MM-Brown', '40MM', 'Brown', 185, 255),
    
    -- 22MM (7 colours)
    ('22MM-White', '22MM', 'White', 110, 160),
    ('22MM-Black', '22MM', 'Black', 110, 160),
    ('22MM-Red', '22MM', 'Red', 115, 165),
    ('22MM-Blue', '22MM', 'Blue', 115, 165),
    ('22MM-Green', '22MM', 'Green', 115, 165),
    ('22MM-Yellow', '22MM', 'Yellow', 115, 165),
    ('22MM-Brown', '22MM', 'Brown', 115, 165),
    
    -- 12MM (2 colours)
    ('12MM-White', '12MM', 'White', 70, 100),
    ('12MM-Black', '12MM', 'Black', 70, 100),
    
    -- 13MM (2 colours)
    ('13MM-White', '13MM', 'White', 75, 110),
    ('13MM-Black', '13MM', 'Black', 75, 110)
ON CONFLICT (size, colour) DO NOTHING;
