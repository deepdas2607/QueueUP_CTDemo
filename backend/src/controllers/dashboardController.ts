import { Response, NextFunction } from 'express';
import { PrismaClient, QueueStatus } from '@prisma/client';
import { AuthenticatedRequest } from '../types';

const prisma = new PrismaClient();

export class DashboardController {
  static async getDashboard(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;

      const totalServices = await prisma.service.count();
      const openServices = await prisma.service.count({ where: { isOpen: true } });

      const activeQueue = await prisma.queueEntry.findFirst({
        where: { userId, status: QueueStatus.WAITING },
        include: { service: true },
      });

      const userStats = {
        totalJoined: await prisma.queueEntry.count({ where: { userId } }),
        completed: await prisma.queueEntry.count({ where: { userId, status: QueueStatus.SERVED } }),
      };

      res.json({
        systemOverview: {
          totalServices,
          openServices,
        },
        hasActiveQueue: Boolean(activeQueue),
        activeQueue: activeQueue || null,
        userStats,
      });
    } catch (error) {
      next(error);
    }
  }
}
