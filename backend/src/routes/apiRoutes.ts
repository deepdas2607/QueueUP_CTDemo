// Defines all REST API endpoints in one single file so routes are easy to find and trace.

import { Router } from 'express';
import { requireAuthToken } from '../middleware/authMiddleware';
import { registerUser, loginUser, getCurrentUser } from '../controllers/authController';
import { getAllServices, getServiceById, getCampusTips } from '../controllers/serviceController';
import {
  joinServiceQueue,
  getActiveQueueForUser,
  refreshAndAdvanceQueue,
  completeServiceQueue,
  leaveServiceQueue,
  getUserQueueHistory,
} from '../controllers/queueController';
import { getUserProfileAndStats, updateUserProfile } from '../controllers/profileController';

const router = Router();

// --- 1. Authentication Routes ---
router.post('/auth/register', registerUser);
router.post('/auth/login', loginUser);
router.get('/auth/me', requireAuthToken, getCurrentUser);

// --- 2. Campus Services Routes ---
router.get('/services', requireAuthToken, getAllServices);
router.get('/services/:id', requireAuthToken, getServiceById);
router.get('/content/queue-tips', requireAuthToken, getCampusTips);

// --- 3. Queue Management Routes ---
router.post('/queues/join', requireAuthToken, joinServiceQueue);
router.get('/queues/my-active', requireAuthToken, getActiveQueueForUser);
router.post('/queues/:id/refresh', requireAuthToken, refreshAndAdvanceQueue);
router.post('/queues/:id/complete', requireAuthToken, completeServiceQueue);
router.post('/queues/:id/leave', requireAuthToken, leaveServiceQueue);
router.get('/queues/history', requireAuthToken, getUserQueueHistory);

// --- 4. User Profile Routes ---
router.get('/profile', requireAuthToken, getUserProfileAndStats);
router.put('/profile', requireAuthToken, updateUserProfile);

export default router;
