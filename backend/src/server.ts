// Main entry point for the QueueUp Express backend server.
// Boots the embedded PostgreSQL database and starts the HTTP server on port 3000.

import express from 'express';
import cors from 'cors';
import { initializeDatabase, startBackgroundQueueTicker } from './database/postgresDatabase';
import apiRoutes from './routes/apiRoutes';

const app = express();
const PORT = Number(process.env.PORT) || 3000;

// Enable Cross-Origin requests and automatic JSON request body parsing.
app.use(cors());
app.use(express.json());

// Logs each incoming API request method, URL, and status code in the terminal.
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    console.log(`[${req.method}] ${req.originalUrl} -> ${res.statusCode} (${Date.now() - start}ms)`);
  });
  next();
});

// Simple health check route to verify the backend server is online.
app.get('/health', (_req, res) => {
  res.json({
    status: 'ok',
    service: 'QueueUp Backend (Embedded PostgreSQL)',
    timestamp: new Date().toISOString(),
  });
});

// Mount all API endpoints under /api.
app.use('/api', apiRoutes);

// Initializes PostgreSQL tables, starts the 30-second simulation ticker, and starts listening on port 3000.
async function startBackendServer() {
  try {
    await initializeDatabase();
    // Start background 30-second ticker to simulate natural campus line flow
    startBackgroundQueueTicker(30000);
    app.listen(PORT, '0.0.0.0', () => {
      console.log(`🚀 QueueUp Express API listening on http://127.0.0.1:${PORT}`);
    });
  } catch (error) {
    console.error('Failed to start QueueUp server:', error);
  }
}

startBackendServer();
