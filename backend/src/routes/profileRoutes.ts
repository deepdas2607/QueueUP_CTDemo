import { Router } from 'express';
import { ProfileController } from '../controllers/profileController';
import { authenticateToken } from '../middleware/authMiddleware';

const router = Router();

router.get('/', authenticateToken, ProfileController.getProfile);
router.put('/', authenticateToken, ProfileController.updateProfile);

export default router;
