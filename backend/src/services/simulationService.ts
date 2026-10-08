// FILE TYPE: Simulation Service
// PURPOSE: Manages realistic campus queue simulation, simulated student traffic, and dynamic queue progression.
// USED BY: QueueService, QueueController, Server Bootstrap
// DATA SOURCE: PostgreSQL Prisma Models

import { PrismaClient, QueueStatus, Role } from '@prisma/client';
import bcrypt from 'bcrypt';

const prisma = new PrismaClient();

const DEFAULT_SERVICES = [
  {
    name: 'College Administration',
    description: 'Transcript requests, fee payment, and official certificates',
    averageServiceTime: 5,
  },
  {
    name: 'Library Help Desk',
    description: 'Book issue, reference assistance, and study room allocation',
    averageServiceTime: 3,
  },
  {
    name: 'Student Services',
    description: 'ID cards, scholarship guidance, and hostel support',
    averageServiceTime: 6,
  },
  {
    name: 'IT Support & Laptop Repair',
    description: 'WiFi access issues, portal password resets, and hardware troubleshooting',
    averageServiceTime: 4,
  },
  {
    name: 'Canteen Counter',
    description: 'Pre-order food collection and digital token redemption',
    averageServiceTime: 2,
  },
  {
    name: 'Financial Aid & Scholarships',
    description: 'Fee concessions, grant processing, and student stipend counseling',
    averageServiceTime: 7,
  },
  {
    name: 'Career Center & Internships',
    description: 'Resume reviews, campus placement schedules, and interview prep',
    averageServiceTime: 8,
  },
  {
    name: 'Health Clinic & First Aid',
    description: 'Doctor consultation, general checkups, and prescription pickup',
    averageServiceTime: 5,
  },
  {
    name: 'Hostel & Accommodation Office',
    description: 'Room allocation, maintenance requests, and night gate passes',
    averageServiceTime: 4,
  },
  {
    name: 'Sports Complex & Equipment',
    description: 'Badminton court booking, gym access pass, and gear checkout',
    averageServiceTime: 3,
  },
  {
    name: 'Bookstore & Stationery Counter',
    description: 'Course textbook pickup, semester lab stationery, and printing tokens',
    averageServiceTime: 2,
  },
  {
    name: 'Examination & Grade Records',
    description: 'Re-evaluation submissions, hall ticket corrections, and grade queries',
    averageServiceTime: 5,
  },
];

const SIMULATED_STUDENTS = [
  { name: 'Alex Taylor', email: 'alex.t@campus.sim', occupation: 'Computer Science Student' },
  { name: 'Samira Khan', email: 'samira.k@campus.sim', occupation: 'Biotech Student' },
  { name: 'Jordan Miller', email: 'jordan.m@campus.sim', occupation: 'Economics Student' },
  { name: 'Priya Patel', email: 'priya.p@campus.sim', occupation: 'Engineering Student' },
  { name: 'Liam O\'Connor', email: 'liam.o@campus.sim', occupation: 'Business Student' },
  { name: 'Ananya Sharma', email: 'ananya.s@campus.sim', occupation: 'Design Student' },
  { name: 'Marcus Chen', email: 'marcus.c@campus.sim', occupation: 'Physics Student' },
  { name: 'Elena Rossi', email: 'elena.r@campus.sim', occupation: 'Architecture Student' },
];

export class SimulationService {
  private static tickerInterval: NodeJS.Timeout | null = null;

  /**
   * Bootstraps 12 campus services and seeds simulated waiting students so queues feel live and populated.
   */
  static async bootstrapSimulatedCampus(): Promise<void> {
    try {
      // 1. Ensure all 12 services exist
      for (const s of DEFAULT_SERVICES) {
        const existing = await prisma.service.findFirst({ where: { name: s.name } });
        if (!existing) {
          await prisma.service.create({
            data: {
              name: s.name,
              description: s.description,
              averageServiceTime: s.averageServiceTime,
              currentWaitingCount: 0,
              isOpen: true,
            },
          });
        }
      }

      // 2. Ensure simulated student accounts exist
      const defaultPasswordHash = await bcrypt.hash('simulated_pass_2026', 8);
      const studentIds: string[] = [];

      for (const st of SIMULATED_STUDENTS) {
        let user = await prisma.user.findUnique({ where: { email: st.email } });
        if (!user) {
          user = await prisma.user.create({
            data: {
              name: st.name,
              email: st.email,
              passwordHash: defaultPasswordHash,
              occupation: st.occupation,
              interests: 'Campus Life, Tech',
              role: Role.USER,
            },
          });
        }
        studentIds.push(user.id);
      }

      // 3. Populate simulated waiting entries for services that have fewer than 2 waiting people
      const services = await prisma.service.findMany();
      for (const service of services) {
        const waitingCount = await prisma.queueEntry.count({
          where: { serviceId: service.id, status: QueueStatus.WAITING },
        });

        if (waitingCount < 2) {
          // Add 2 to 4 simulated people
          const targetWaiting = Math.floor(Math.random() * 3) + 2; // 2, 3, or 4
          for (let i = waitingCount; i < targetWaiting; i++) {
            const studentId = studentIds[(i + service.name.length) % studentIds.length];
            // Check if this student already has a waiting entry in this service
            const alreadyWaiting = await prisma.queueEntry.findFirst({
              where: { userId: studentId, serviceId: service.id, status: QueueStatus.WAITING },
            });

            if (!alreadyWaiting) {
              const joinedTime = new Date(Date.now() - (targetWaiting - i) * service.averageServiceTime * 60 * 1000);
              await prisma.queueEntry.create({
                data: {
                  userId: studentId,
                  serviceId: service.id,
                  position: i + 1,
                  status: QueueStatus.WAITING,
                  joinedAt: joinedTime,
                },
              });
            }
          }
        }

        // Synchronize service's currentWaitingCount with actual database WAITING entries
        const finalCount = await prisma.queueEntry.count({
          where: { serviceId: service.id, status: QueueStatus.WAITING },
        });

        await prisma.service.update({
          where: { id: service.id },
          data: { currentWaitingCount: finalCount },
        });
      }

      console.log(`🎓 Campus Services & Simulated Queues successfully initialized (${services.length} counters active).`);
    } catch (err: any) {
      console.error('Error bootstrapping simulated campus:', err.message || err);
    }
  }

  /**
   * Advances the queue for a user by serving one simulated person waiting ahead of them.
   * Called on queue refresh so the user dynamically sees their position decrease.
   */
  static async advanceUserQueue(userId: string): Promise<boolean> {
    try {
      const activeEntry = await prisma.queueEntry.findFirst({
        where: { userId, status: QueueStatus.WAITING },
        include: { service: true },
      });

      if (!activeEntry) return false;

      // Find the earliest simulated waiting person ahead of this user
      const personAhead = await prisma.queueEntry.findFirst({
        where: {
          serviceId: activeEntry.serviceId,
          status: QueueStatus.WAITING,
          joinedAt: { lt: activeEntry.joinedAt },
          user: {
            email: { endsWith: '@campus.sim' },
          },
        },
        orderBy: { joinedAt: 'asc' },
      });

      if (personAhead) {
        // Serve this simulated person
        await prisma.queueEntry.update({
          where: { id: personAhead.id },
          data: {
            status: QueueStatus.SERVED,
            servedAt: new Date(),
          },
        });

        // Update service waiting count
        const newWaitingCount = await prisma.queueEntry.count({
          where: { serviceId: activeEntry.serviceId, status: QueueStatus.WAITING },
        });

        await prisma.service.update({
          where: { id: activeEntry.serviceId },
          data: { currentWaitingCount: newWaitingCount },
        });

        return true;
      }

      return false;
    } catch (err: any) {
      console.error('Error advancing user queue:', err.message || err);
      return false;
    }
  }

  /**
   * Background ticker that simulates natural campus flow every 30 seconds.
   */
  static startBackgroundTicker(intervalMs: number = 30000): void {
    if (this.tickerInterval) return;

    this.tickerInterval = setInterval(async () => {
      try {
        // Pick one random service that has simulated users waiting
        const services = await prisma.service.findMany({
          where: { isOpen: true, currentWaitingCount: { gt: 0 } },
        });

        if (services.length > 0) {
          const randomService = services[Math.floor(Math.random() * services.length)];
          const earliestSim = await prisma.queueEntry.findFirst({
            where: {
              serviceId: randomService.id,
              status: QueueStatus.WAITING,
              user: { email: { endsWith: '@campus.sim' } },
            },
            orderBy: { joinedAt: 'asc' },
          });

          if (earliestSim) {
            await prisma.queueEntry.update({
              where: { id: earliestSim.id },
              data: { status: QueueStatus.SERVED, servedAt: new Date() },
            });

            const newCount = await prisma.queueEntry.count({
              where: { serviceId: randomService.id, status: QueueStatus.WAITING },
            });

            await prisma.service.update({
              where: { id: randomService.id },
              data: { currentWaitingCount: newCount },
            });
          }
        }
      } catch (err: any) {
        // Background ticker non-blocking catch
      }
    }, intervalMs);
  }

  static stopBackgroundTicker(): void {
    if (this.tickerInterval) {
      clearInterval(this.tickerInterval);
      this.tickerInterval = null;
    }
  }
}
