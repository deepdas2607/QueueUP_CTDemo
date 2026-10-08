import { Request, Response, NextFunction } from 'express';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

export class ServiceController {
  static async getAllServices(req: Request, res: Response, next: NextFunction) {
    try {
      const services = await prisma.service.findMany({
        orderBy: { name: 'asc' },
      });
      res.json(services);
    } catch (error) {
      next(error);
    }
  }

  static async getServiceById(req: Request, res: Response, next: NextFunction) {
    try {
      const id = String(req.params.id);
      const service = await prisma.service.findUnique({
        where: { id },
      });
      if (!service) {
        res.status(404).json({ error: 'Service not found' });
        return;
      }
      res.json(service);
    } catch (error) {
      next(error);
    }
  }
}
