import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types';

export const requireAdmin = (req: AuthenticatedRequest, res: Response, next: NextFunction): void => {
  if (!req.user || req.user.role !== 'ADMIN') {
    res.status(403).json({ error: 'Admin authorization required' });
    return;
  }
  next();
};
