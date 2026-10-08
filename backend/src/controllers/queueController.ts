import { Response, NextFunction } from 'express';
import { QueueService } from '../services/queueService';
import { AuthenticatedRequest } from '../types';
import { logAuditAction } from '../services/auditService';

export class QueueController {
  static async joinQueue(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { serviceId } = req.body;
      if (!serviceId) {
        res.status(400).json({ error: 'serviceId is required' });
        return;
      }
      const result = await QueueService.joinQueue(userId, serviceId);
      await logAuditAction(userId, 'USER_JOINED_QUEUE', 'QueueEntry', result.queueEntry.id);
      res.status(201).json(result);
    } catch (error) {
      next(error);
    }
  }

  static async getMyActiveQueue(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const active = await QueueService.getActiveQueue(userId);
      res.json({ activeQueue: active });
    } catch (error) {
      next(error);
    }
  }

  static async getQueueById(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const id = String(req.params.id);
      const active = await QueueService.getActiveQueue(userId);
      if (active && active.queueEntry.id === id) {
        res.json(active);
        return;
      }
      res.status(404).json({ error: 'Queue entry not found or inactive' });
    } catch (error) {
      next(error);
    }
  }

  static async leaveQueue(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const id = String(req.params.id);
      const updated = await QueueService.leaveQueue(userId, id);
      await logAuditAction(userId, 'USER_LEFT_QUEUE', 'QueueEntry', id);
      res.json(updated);
    } catch (error) {
      next(error);
    }
  }

  static async refreshQueue(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const active = await QueueService.getActiveQueue(userId);
      res.json({ activeQueue: active });
    } catch (error) {
      next(error);
    }
  }

  static async getHistory(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const history = await QueueService.getHistory(userId);
      res.json(history);
    } catch (error) {
      next(error);
    }
  }

  static async adminServeQueue(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const adminId = req.user!.userId;
      const id = String(req.params.id);
      const updated = await QueueService.adminServeQueue(adminId, id);
      await logAuditAction(adminId, 'ADMIN_SERVED_QUEUE', 'QueueEntry', id);
      res.json(updated);
    } catch (error) {
      next(error);
    }
  }

  static async adminCancelQueue(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const adminId = req.user!.userId;
      const id = String(req.params.id);
      const updated = await QueueService.adminCancelQueue(adminId, id);
      await logAuditAction(adminId, 'ADMIN_CANCELLED_QUEUE', 'QueueEntry', id);
      res.json(updated);
    } catch (error) {
      next(error);
    }
  }
}

