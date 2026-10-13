// Handles loading and updating the user's profile information and queue statistics.

import { Response } from 'express';
import {
  findUserById,
  updateUserDetails,
  getUserQueueStats,
} from '../database/postgresDatabase';
import { AuthRequest } from '../middleware/authMiddleware';

// Returns the user's profile details along with their total joined, completed, and cancelled counts.
export async function getUserProfileAndStats(req: AuthRequest, res: Response): Promise<void> {
  try {
    const user = await findUserById(req.userId!);
    if (!user) {
      res.status(404).json({ error: 'User profile not found' });
      return;
    }
    const stats = await getUserQueueStats(req.userId!);
    res.json({ user, stats });
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to load profile' });
  }
}

// Updates the user's name, phone, occupation, and interests in PostgreSQL.
export async function updateUserProfile(req: AuthRequest, res: Response): Promise<void> {
  try {
    const { name, phone, occupation, interests } = req.body;
    const updatedUser = await updateUserDetails(req.userId!, {
      name,
      phone,
      occupation,
      interests,
    });
    if (!updatedUser) {
      res.status(404).json({ error: 'User profile not found' });
      return;
    }
    res.json(updatedUser);
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to update profile' });
  }
}
