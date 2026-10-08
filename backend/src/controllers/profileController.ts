import { Response, NextFunction } from 'express';
import { PrismaClient } from '@prisma/client';
import { AuthenticatedRequest, QueueStatus } from '../types';

const prisma = new PrismaClient();

export class ProfileController {
  static async getProfile(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const user = await prisma.user.findUnique({
        where: { id: userId },
      });

      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }

      const totalJoined = await prisma.queueEntry.count({ where: { userId } });
      const completed = await prisma.queueEntry.count({
        where: { userId, status: QueueStatus.SERVED },
      });
      const cancelled = await prisma.queueEntry.count({
        where: { userId, status: QueueStatus.CANCELLED },
      });

      const { passwordHash: _, ...safeUser } = user;
      res.json({
        user: safeUser,
        stats: {
          totalJoined,
          completed,
          cancelled,
        },
      });
    } catch (error) {
      next(error);
    }
  }

  static async updateProfile(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { name, occupation, interests } = req.body;

      const updatedUser = await prisma.user.update({
        where: { id: userId },
        data: {
          ...(name && { name }),
          ...(occupation !== undefined && { occupation }),
          ...(interests !== undefined && { interests }),
        },
      });

      const { passwordHash: _, ...safeUser } = updatedUser;
      res.json(safeUser);
    } catch (error) {
      next(error);
    }
  }
}
