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

// Seed Default Services if empty
const seedServices = async () => {
  try {
    const count = await prisma.service.count();
    if (count === 0) {
      console.log('Seeding initial campus services...');
      await prisma.service.createMany({
        data: [
          {
            name: 'College Administration',
            description: 'Transcript requests, fee payment, and official certificates',
            averageServiceTime: 5,
            currentWaitingCount: 0,
            isOpen: true,
          },
          {
            name: 'Library Help Desk',
            description: 'Book issue, reference assistance, and study room allocation',
            averageServiceTime: 3,
            currentWaitingCount: 0,
            isOpen: true,
          },
          {
            name: 'Student Services',
            description: 'ID cards, scholarship guidance, and hostel support',
            averageServiceTime: 6,
            currentWaitingCount: 0,
            isOpen: true,
          },
          {
            name: 'IT Support',
            description: 'WiFi access issues, portal password resets, and hardware help',
            averageServiceTime: 4,
            currentWaitingCount: 0,
            isOpen: true,
          },
          {
            name: 'Canteen Counter',
            description: 'Pre-order food collection and digital token redemption',
            averageServiceTime: 2,
            currentWaitingCount: 0,
            isOpen: true,
          },
        ],
      });
      console.log('Services seeded successfully.');
    }
  } catch (error) {
    console.error('Service Seeding Status:', (error as Error).message || error);
  }
};

const startServer = async () => {
  try {
    await startLocalPostgres();
    app.listen(config.port, '0.0.0.0', async () => {
      console.log(`QueueUp Express REST API running on port ${config.port}`);
      await seedServices();
    });
  } catch (error) {
    console.error('Server startup error:', error);
  }
};

startServer();

