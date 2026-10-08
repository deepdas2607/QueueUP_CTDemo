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

const app = express();
const prisma = new PrismaClient();

app.use(cors());
app.use(express.json());

// Request Logger
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    console.log(`[${req.method}] ${req.originalUrl} -> ${res.statusCode} (${Date.now() - start}ms) [from ${req.ip}]`);
  });
  next();
});

// Health Check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'QueueUp Backend (PostgreSQL)', timestamp: new Date() });
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/services', serviceRoutes);
app.use('/api/queues', queueRoutes);
app.use('/api/profile', profileRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/content', contentRoutes);

// Error Middleware
app.use(errorHandler);

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

