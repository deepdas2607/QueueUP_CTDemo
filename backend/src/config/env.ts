import dotenv from 'dotenv';
dotenv.config();

export const config = {
  port: parseInt(process.env.PORT || '3000', 10),
  databaseUrl: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/queueup_db?schema=public',
  jwtSecret: process.env.JWT_SECRET || 'queueup_super_secret_jwt_key_2026',
  cleverTapAccountId: process.env.CLEVERTAP_ACCOUNT_ID || '',
  cleverTapPasscode: process.env.CLEVERTAP_PASSCODE || '',
};
