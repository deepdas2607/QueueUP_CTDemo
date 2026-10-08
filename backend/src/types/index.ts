import { Request } from 'express';

export type Role = 'USER' | 'ADMIN';
export const Role = {
  USER: 'USER' as const,
  ADMIN: 'ADMIN' as const,
};

export type QueueStatus = 'WAITING' | 'SERVED' | 'CANCELLED';
export const QueueStatus = {
  WAITING: 'WAITING' as const,
  SERVED: 'SERVED' as const,
  CANCELLED: 'CANCELLED' as const,
};

export interface JwtPayload {
  userId: string;
  email: string;
  role: Role;
}

export interface AuthenticatedRequest extends Request {
  user?: JwtPayload;
}
