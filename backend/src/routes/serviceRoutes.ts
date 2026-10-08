import { Router } from 'express';
import { ServiceController } from '../controllers/serviceController';
import { authenticateToken } from '../middleware/authMiddleware';

const router = Router();

router.get('/', authenticateToken, ServiceController.getAllServices);
router.get('/:id', authenticateToken, ServiceController.getServiceById);

export default router;
