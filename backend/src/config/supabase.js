const { createClient } = require('@supabase/supabase-js');
const config = require('./environment');

if (!config.supabase.url || !config.supabase.serviceRoleKey) {
  console.warn('Supabase URL or Service Role Key is missing. Database connection will fail.');
}

const supabase = createClient(
  config.supabase.url || 'https://placeholder.supabase.co',
  config.supabase.serviceRoleKey || 'placeholder_key',
  {
    auth: {
      autoRefreshToken: false,
      persistSession: false
    }
  }
);

module.exports = supabase;
