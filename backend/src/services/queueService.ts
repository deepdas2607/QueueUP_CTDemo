import { PrismaClient } from '@prisma/client';
import { QueueStatus } from '../types';

const prisma = new PrismaClient();

export class QueueService {
  static async joinQueue(userId: string, serviceId: string) {
    const service = await prisma.service.findUnique({
      where: { id: serviceId },
    });

    if (!service) {
      throw { status: 404, message: 'Service not found' };
    }

    if (!service.isOpen) {
      throw { status: 400, message: 'Service is currently closed' };
    }

    // Check if user already has an active queue for any service or this service
    const existingActive = await prisma.queueEntry.findFirst({
      where: {
        userId,
        status: QueueStatus.WAITING,
      },
      include: { service: true },
    });

    if (existingActive) {
      throw { status: 400, message: `You are already in queue for ${existingActive.service.name}` };
    }

    // Calculate position based on current WAITING count for this service
    const waitingCount = await prisma.queueEntry.count({
      where: {
        serviceId,
        status: QueueStatus.WAITING,
      },
    });

    const position = waitingCount + 1;

    const queueEntry = await prisma.queueEntry.create({
      data: {
        userId,
        serviceId,
        position,
        status: QueueStatus.WAITING,
      },
      include: {
        service: true,
      },
    });

    // Update service currentWaitingCount
    await prisma.service.update({
      where: { id: serviceId },
      data: { currentWaitingCount: position },
    });

    const estimatedWaitMinutes = position * service.averageServiceTime;

    return {
      queueEntry,
      estimatedWaitMinutes,
      peopleAhead: position - 1,
    };
  }

  static async getActiveQueue(userId: string) {
    const active = await prisma.queueEntry.findFirst({
      where: {
        userId,
        status: QueueStatus.WAITING,
      },
      include: {
        service: true,
      },
    });

    if (!active) {
      return null;
    }

    // Calculate current people ahead
    const peopleAhead = await prisma.queueEntry.count({
      where: {
        serviceId: active.serviceId,
        status: QueueStatus.WAITING,
        joinedAt: {
          lt: active.joinedAt,
        },
      },
    });

    const currentPosition = peopleAhead + 1;
    const estimatedWaitMinutes = currentPosition * active.service.averageServiceTime;

    return {
      queueEntry: {
        ...active,
        position: currentPosition,
      },
      peopleAhead,
      estimatedWaitMinutes,
    };
  }

  static async leaveQueue(userId: string, queueEntryId: string) {
    const entry = await prisma.queueEntry.findUnique({
      where: { id: queueEntryId },
    });

    if (!entry) {
      throw { status: 404, message: 'Queue entry not found' };
    }

    if (entry.userId !== userId) {
      throw { status: 403, message: 'Unauthorized to leave this queue' };
    }

    if (entry.status !== QueueStatus.WAITING) {
      throw { status: 400, message: 'Queue entry is no longer active' };
    }

    const updated = await prisma.queueEntry.update({
      where: { id: queueEntryId },
      data: {
        status: QueueStatus.CANCELLED,
        cancelledAt: new Date(),
      },
      include: { service: true },
    });

    // Update currentWaitingCount for service
    const remainingWaiting = await prisma.queueEntry.count({
      where: {
        serviceId: entry.serviceId,
        status: QueueStatus.WAITING,
      },
    });

    await prisma.service.update({
      where: { id: entry.serviceId },
      data: { currentWaitingCount: remainingWaiting },
    });

    return updated;
  }

  static async getHistory(userId: string) {
    return prisma.queueEntry.findMany({
      where: { userId },
      orderBy: { joinedAt: 'desc' },
      include: { service: true },
    });
  }
}
