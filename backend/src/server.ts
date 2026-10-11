// FILE TYPE: Express Server Entrypoint
// PURPOSE: Initializes PostgreSQL engine, configures Express REST API middleware, mounts routes, and seeds campus data.
// USED BY: Node.js runtime (`node dist/server.js` or `npm run dev`)
// CALLS: dbLocal, PrismaClient, SimulationService, Route handlers

import { startLocalPostgres } from './dbLocal';
import express from 'express';
import cors from 'cors';
import { PrismaClient } from '@prisma/client';
import { config } from './config/env';

import authRoutes from './routes/authRoutes';
import serviceRoutes from './routes/serviceRoutes';
import queueRoutes from './routes/queueRoutes';
import profileRoutes from './routes/profileRoutes';
import dashboardRoutes from './routes/dashboardRoutes';
import contentRoutes from './routes/contentRoutes';
import { errorHandler } from './middleware/errorHandler';
import { SimulationService } from './services/simulationService';

// Initialize Express web server application
const app = express();
const prisma = new PrismaClient();

// Cross-Origin Resource Sharing (allows web clients or emulators to query endpoints)
app.use(cors());

// JSON middleware parses incoming `application/json` request bodies into req.body
app.use(express.json());

// Request Logger Middleware: logs HTTP method, endpoint, status code, latency, and client IP
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    console.log(`[${req.method}] ${req.originalUrl} -> ${res.statusCode} (${Date.now() - start}ms) [from ${req.ip}]`);
  });
  next();
});

// Non-destructive Health Check endpoint for uptime monitors and container orchestration
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'QueueUp Backend (PostgreSQL)', timestamp: new Date() });
});

// REST API Route Mounts
app.use('/api/auth', authRoutes);         // POST /register, POST /login
app.use('/api/services', serviceRoutes); // GET /, GET /:id
app.use('/api/queues', queueRoutes);     // POST /join, GET /active, POST /leave, GET /history, POST /refresh
app.use('/api/profile', profileRoutes);   // GET /, PUT /
app.use('/api/dashboard', dashboardRoutes); // GET /
app.use('/api/content', contentRoutes);   // GET /tips

// Centralized error-handling middleware catches unhandled exceptions and returns clean JSON
app.use(errorHandler);

/**
 * Server startup lifecycle:
 * 1. Boots embedded PostgreSQL socket engine on 127.0.0.1:5432.
 * 2. Starts Express HTTP listener on port 3000 (bound to 0.0.0.0 for emulator compatibility).
 * 3. Bootstraps 12 campus counters and realistic student queue traffic in the database.
 * 4. Activates background ticker simulating real-world queue processing every 30 seconds.
 */
const startServer = async () => {
  try {
    await startLocalPostgres();
    app.listen(config.port, '0.0.0.0', async () => {
      console.log(`QueueUp Express REST API running on port ${config.port}`);
      // Bootstrap 12 campus counters and realistic simulated queues
      await SimulationService.bootstrapSimulatedCampus();
      // Start natural background queue movement simulation (every 30s)
      SimulationService.startBackgroundTicker(30000);
    });
  } catch (error) {
    console.error('Server startup error:', error);
  }
};

startServer();


