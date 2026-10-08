import { Router } from 'express';
import { QueueController } from '../controllers/queueController';
import { authenticateToken } from '../middleware/authMiddleware';

const router = Router();

router.post('/join', authenticateToken, QueueController.joinQueue);
router.get('/my-active', authenticateToken, QueueController.getMyActiveQueue);
router.get('/history', authenticateToken, QueueController.getHistory);
router.get('/:id', authenticateToken, QueueController.getQueueById);
router.post('/:id/leave', authenticateToken, QueueController.leaveQueue);
router.post('/:id/refresh', authenticateToken, QueueController.refreshQueue);

export default router;
