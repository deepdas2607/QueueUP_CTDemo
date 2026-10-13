// Handles joining a queue, checking active ticket status, advancing simulated turns, completing service, and leaving a queue.

import { Response } from 'express';
import {
  joinCampusServiceQueue,
  getActiveQueueEntryForUser,
  advanceQueueForUser,
  completeQueueForUser,
  leaveQueueForUser,
  getQueueHistoryForUser,
} from '../database/postgresDatabase';
import { AuthRequest } from '../middleware/authMiddleware';

// Enters the logged-in user into a campus counter queue and returns their ticket position.
export async function joinServiceQueue(req: AuthRequest, res: Response): Promise<void> {
  try {
    const { serviceId } = req.body;
    if (!serviceId) {
      res.status(400).json({ error: 'serviceId is required to join a queue' });
      return;
    }
    const activeData = await joinCampusServiceQueue(req.userId!, serviceId);
    res.status(201).json(activeData);
  } catch (error: any) {
    res.status(400).json({ error: error.message || 'Failed to join queue' });
  }
}

// Returns the user's current active queue ticket (or null if they are not in any line).
export async function getActiveQueueForUser(req: AuthRequest, res: Response): Promise<void> {
  try {
    const activeQueue = await getActiveQueueEntryForUser(req.userId!);
    res.json({ activeQueue });
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to get active queue' });
  }
}

// Advances the simulated line by 1 person when the user taps Refresh so they can test reaching #1.
export async function refreshAndAdvanceQueue(req: AuthRequest, res: Response): Promise<void> {
  try {
    await advanceQueueForUser(req.userId!);
    const activeQueue = await getActiveQueueEntryForUser(req.userId!);
    res.json({ activeQueue });
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to refresh queue' });
  }
}

// Marks the user's queue ticket as SERVED when they finish at the counter.
export async function completeServiceQueue(req: AuthRequest, res: Response): Promise<void> {
  try {
    const queueId = String(req.params.id);
    const completed = await completeQueueForUser(req.userId!, queueId);
    res.json(completed);
  } catch (error: any) {
    res.status(400).json({ error: error.message || 'Failed to complete queue' });
  }
}

// Cancels the user's queue ticket when they tap Leave Queue.
export async function leaveServiceQueue(req: AuthRequest, res: Response): Promise<void> {
  try {
    const queueId = String(req.params.id);
    const cancelled = await leaveQueueForUser(req.userId!, queueId);
    res.json(cancelled);
  } catch (error: any) {
    res.status(400).json({ error: error.message || 'Failed to leave queue' });
  }
}

// Returns the list of past completed and cancelled queue tickets for the user.
export async function getUserQueueHistory(req: AuthRequest, res: Response): Promise<void> {
  try {
    const history = await getQueueHistoryForUser(req.userId!);
    res.json(history);
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to load queue history' });
  }
}
