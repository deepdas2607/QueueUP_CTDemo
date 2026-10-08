import { Request } from 'express';
import { Role, QueueStatus } from '@prisma/client';

export { Role, QueueStatus };

export interface JwtPayload {
  userId: string;
  email: string;
  role: Role;
}

export interface AuthenticatedRequest extends Request {
  user?: JwtPayload;
}
