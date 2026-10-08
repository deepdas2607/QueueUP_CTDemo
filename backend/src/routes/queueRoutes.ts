// FILE TYPE: Routes
// PURPOSE: Defines queue management API endpoints (joining, viewing, canceling, refreshing, history, and admin operations).
// USED BY: server.ts
// CALLS: QueueController, authMiddleware, adminMiddleware

import { Router } from 'express';
import { QueueController } from '../controllers/queueController';
import { authenticateToken } from '../middleware/authMiddleware';
import { requireAdmin } from '../middleware/adminMiddleware';

const router = Router();

// User Queue Endpoints
router.post('/join', authenticateToken, QueueController.joinQueue);
router.get('/my-active', authenticateToken, QueueController.getMyActiveQueue);
router.get('/history', authenticateToken, QueueController.getHistory);
router.get('/:id', authenticateToken, QueueController.getQueueById);
router.post('/:id/leave', authenticateToken, QueueController.leaveQueue);
router.post('/:id/refresh', authenticateToken, QueueController.refreshQueue);

// Admin Queue Management Endpoints
router.post('/:id/serve', authenticateToken, requireAdmin, QueueController.adminServeQueue);
router.post('/:id/admin-cancel', authenticateToken, requireAdmin, QueueController.adminCancelQueue);

export default router;
