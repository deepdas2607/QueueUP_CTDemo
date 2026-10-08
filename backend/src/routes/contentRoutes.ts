import { Router } from 'express';
import { ContentController } from '../controllers/contentController';

const router = Router();

router.get('/queue-tips', ContentController.getQueueTips);

export default router;
