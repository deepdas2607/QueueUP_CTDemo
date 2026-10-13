// Runs embedded PostgreSQL directly inside Node.js without needing an external database server or TCP socket.
// Stores real SQL tables (users, services, queue_entries) on disk and seeds 12 campus counters.

import { PGlite } from '@electric-sql/pglite';
import bcrypt from 'bcrypt';
import crypto from 'crypto';
import fs from 'fs';
import path from 'path';

// Holds the active embedded PostgreSQL database instance.
let db: PGlite;

// Default 12 campus counters seeded into the SQL database on startup.
const CAMPUS_SERVICES = [
  {
    name: 'College Administration',
    category: 'Admin & Finance',
    description: 'Transcript requests, fee payment receipts, and official certificates',
    averageServiceTime: 5,
  },
  {
    name: 'Library Help Desk',
    category: 'Academic',
    description: 'Book issue, reference assistance, and study room allocation',
    averageServiceTime: 3,
  },
  {
    name: 'Student Services',
    category: 'Campus Life',
    description: 'Student ID cards, club registrations, and campus pass renewals',
    averageServiceTime: 6,
  },
  {
    name: 'IT Support & Laptop Repair',
    category: 'Tech & Health',
    description: 'WiFi access issues, portal password resets, and hardware checks',
    averageServiceTime: 4,
  },
  {
    name: 'Canteen Counter',
    category: 'Campus Life',
    description: 'Pre-order meal collection and digital token redemption',
    averageServiceTime: 2,
  },
  {
    name: 'Financial Aid & Scholarships',
    category: 'Admin & Finance',
    description: 'Fee concessions, grant processing, and student stipend counseling',
    averageServiceTime: 7,
  },
  {
    name: 'Career Center & Internships',
    category: 'Academic',
    description: 'Resume reviews, campus placement schedules, and interview prep',
    averageServiceTime: 8,
  },
  {
    name: 'Health Clinic & First Aid',
    category: 'Tech & Health',
    description: 'Doctor consultation, general checkups, and prescription pickup',
    averageServiceTime: 5,
  },
  {
    name: 'Hostel & Accommodation Office',
    category: 'Campus Life',
    description: 'Room allocation, maintenance requests, and night gate passes',
    averageServiceTime: 4,
  },
  {
    name: 'Sports Complex & Equipment',
    category: 'Campus Life',
    description: 'Badminton court booking, gym access pass, and gear checkout',
    averageServiceTime: 3,
  },
  {
    name: 'Bookstore & Stationery Counter',
    category: 'Academic',
    description: 'Course textbook pickup, semester lab stationery, and printing tokens',
    averageServiceTime: 2,
  },
  {
    name: 'Examination & Grade Records',
    category: 'Academic',
    description: 'Re-evaluation submissions, hall ticket corrections, and grade queries',
    averageServiceTime: 5,
  },
];

// Simulated students used to populate queues with realistic waiting numbers.
const SIMULATED_STUDENTS = [
  { name: 'Samira Khan', email: 'samira.k@campus.sim', occupation: 'Biotech Student' },
  { name: 'Jordan Miller', email: 'jordan.m@campus.sim', occupation: 'Economics Student' },
  { name: 'Priya Patel', email: 'priya.p@campus.sim', occupation: 'Engineering Student' },
  { name: 'Liam OConnor', email: 'liam.o@campus.sim', occupation: 'Business Student' },
  { name: 'Ananya Sharma', email: 'ananya.s@campus.sim', occupation: 'Design Student' },
  { name: 'Marcus Chen', email: 'marcus.c@campus.sim', occupation: 'Physics Student' },
];

// Creates a random UUID string for new database records.
function createId(): string {
  return crypto.randomUUID();
}

// Boots the embedded PostgreSQL database, creates SQL tables, and seeds initial campus data.
export async function initializeDatabase(): Promise<void> {
  const dataDir = path.resolve(__dirname, '../../.postgres-db');

  // Remove any leftover lock file from a previous crash so startup never fails.
  const lockFile = path.join(dataDir, 'postmaster.pid');
  if (fs.existsSync(lockFile)) {
    try {
      fs.unlinkSync(lockFile);
    } catch (_) {}
  }

  try {
    db = new PGlite(dataDir);
    await db.waitReady;
  } catch (err) {
    // Fallback to in-memory PostgreSQL if disk folder is ever locked by another process.
    console.warn('Disk folder busy, starting clean in-memory PostgreSQL instance...');
    db = new PGlite();
    await db.waitReady;
  }

  // Create SQL tables for users, services, and queue entries.
  await db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      phone TEXT DEFAULT '',
      occupation TEXT DEFAULT 'Student',
      interests TEXT DEFAULT 'Campus Life',
      role TEXT DEFAULT 'USER',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS services (
      id TEXT PRIMARY KEY,
      name TEXT UNIQUE NOT NULL,
      category TEXT DEFAULT 'General',
      description TEXT NOT NULL,
      average_service_time INTEGER NOT NULL,
      current_waiting_count INTEGER DEFAULT 0,
      is_open BOOLEAN DEFAULT TRUE
    );

    CREATE TABLE IF NOT EXISTS queue_entries (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      service_id TEXT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
      position INTEGER NOT NULL,
      status TEXT NOT NULL DEFAULT 'WAITING',
      joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      served_at TIMESTAMP,
      cancelled_at TIMESTAMP
    );
  `);

  // Ensure phone and category columns exist if upgrading from an older table schema.
  await db.exec(`
    ALTER TABLE users ADD COLUMN IF NOT EXISTS phone TEXT DEFAULT '';
    ALTER TABLE services ADD COLUMN IF NOT EXISTS category TEXT DEFAULT 'General';
  `);

  await seedInitialCampusData();
  console.log('✅ Embedded PostgreSQL Database ready (12 campus counters active).');
}

// Seeds demo accounts, 12 campus services, and simulated waiting students into PostgreSQL.
async function seedInitialCampusData(): Promise<void> {
  // 1. Seed default login accounts for quick testing.
  const alexPass = await bcrypt.hash('password123', 8);
  const deepPass = await bcrypt.hash('Password123!', 8);

  await db.query(
    `INSERT INTO users (id, name, email, password_hash, phone, occupation, interests, role)
     VALUES ($1, $2, $3, $4, $5, $6, $7, 'USER')
     ON CONFLICT (email) DO NOTHING`,
    [createId(), 'Alex', 'a@b.com', alexPass, '+919876543210', 'Computer Science Student', 'Tech, Library, Coding']
  );

  await db.query(
    `INSERT INTO users (id, name, email, password_hash, phone, occupation, interests, role)
     VALUES ($1, $2, $3, $4, $5, $6, $7, 'USER')
     ON CONFLICT (email) DO NOTHING`,
    [createId(), 'Deep Kumar', 'deep@test.edu', deepPass, '+919876500001', 'Engineering Student', 'AI, Analytics, Campus']
  );

  // 2. Seed simulated student users.
  const simPass = await bcrypt.hash('sim_pass_123', 8);
  const simUserIds: string[] = [];

  for (const student of SIMULATED_STUDENTS) {
    await db.query(
      `INSERT INTO users (id, name, email, password_hash, occupation, interests, role)
       VALUES ($1, $2, $3, $4, $5, 'Campus Life', 'USER')
       ON CONFLICT (email) DO NOTHING`,
      [createId(), student.name, student.email, simPass, student.occupation]
    );
    const res = await db.query<{ id: string }>(`SELECT id FROM users WHERE email = $1`, [student.email]);
    if (res.rows[0]) simUserIds.push(res.rows[0].id);
  }

  // 3. Seed all 12 campus services.
  for (const service of CAMPUS_SERVICES) {
    await db.query(
      `INSERT INTO services (id, name, category, description, average_service_time, current_waiting_count, is_open)
       VALUES ($1, $2, $3, $4, $5, 0, TRUE)
       ON CONFLICT (name) DO UPDATE SET category = EXCLUDED.category`,
      [createId(), service.name, service.category, service.description, service.averageServiceTime]
    );
  }

  // 4. Populate each service with 2 to 4 simulated waiting students so queues never look empty.
  const servicesRes = await db.query<{ id: string; name: string; average_service_time: number }>(
    `SELECT id, name, average_service_time FROM services`
  );

  for (const service of servicesRes.rows) {
    const countRes = await db.query<{ count: string }>(
      `SELECT COUNT(*) AS count FROM queue_entries WHERE service_id = $1 AND status = 'WAITING'`,
      [service.id]
    );
    const currentWaiting = parseInt(countRes.rows[0]?.count || '0', 10);

    if (currentWaiting < 2 && simUserIds.length > 0) {
      const targetWaiting = (service.name.length % 3) + 2; // 2, 3, or 4 waiting people
      for (let i = currentWaiting; i < targetWaiting; i++) {
        const simUserId = simUserIds[(i + service.name.length) % simUserIds.length];
        const joinedAt = new Date(Date.now() - (targetWaiting - i) * 180000).toISOString();
        await db.query(
          `INSERT INTO queue_entries (id, user_id, service_id, position, status, joined_at)
           VALUES ($1, $2, $3, $4, 'WAITING', $5)`,
          [createId(), simUserId, service.id, i + 1, joinedAt]
        );
      }
    }

    await recalculateServiceWaitingCount(service.id);
  }
}

// Recalculates positions (1, 2, 3...) and updates the waiting counter for a service.
export async function recalculateServiceWaitingCount(serviceId: string): Promise<void> {
  const waitingRows = await db.query<{ id: string }>(
    `SELECT id FROM queue_entries WHERE service_id = $1 AND status = 'WAITING' ORDER BY joined_at ASC`,
    [serviceId]
  );

  for (let i = 0; i < waitingRows.rows.length; i++) {
    await db.query(`UPDATE queue_entries SET position = $1 WHERE id = $2`, [i + 1, waitingRows.rows[i].id]);
  }

  await db.query(`UPDATE services SET current_waiting_count = $1 WHERE id = $2`, [
    waitingRows.rows.length,
    serviceId,
  ]);
}

// Finds a user record by email address for login and registration checks.
export async function findUserByEmail(email: string) {
  const result = await db.query<any>(`SELECT * FROM users WHERE LOWER(email) = LOWER($1) LIMIT 1`, [email]);
  return result.rows[0] || null;
}

// Finds a user record by their ID for profile lookup.
export async function findUserById(userId: string) {
  const result = await db.query<any>(`SELECT * FROM users WHERE id = $1 LIMIT 1`, [userId]);
  if (!result.rows[0]) return null;
  const row = result.rows[0];
  return {
    id: row.id,
    name: row.name,
    email: row.email,
    phone: row.phone || '',
    occupation: row.occupation || 'Student',
    interests: row.interests || 'Campus Life',
    role: row.role || 'USER',
    createdAt: row.created_at,
  };
}

// Inserts a new user into the PostgreSQL users table.
export async function createNewUser(data: {
  name: string;
  email: string;
  passwordHash: string;
  phone?: string;
  occupation?: string;
  interests?: string;
}) {
  const id = createId();
  await db.query(
    `INSERT INTO users (id, name, email, password_hash, phone, occupation, interests, role)
     VALUES ($1, $2, $3, $4, $5, $6, $7, 'USER')`,
    [
      id,
      data.name,
      data.email.toLowerCase(),
      data.passwordHash,
      data.phone || '',
      data.occupation || 'Student',
      data.interests || 'Campus Life',
    ]
  );
  return findUserById(id);
}

// Updates a user's profile fields (name, phone, occupation, interests) in PostgreSQL.
export async function updateUserDetails(
  userId: string,
  updates: { name?: string; phone?: string; occupation?: string; interests?: string }
) {
  const existing = await findUserById(userId);
  if (!existing) return null;

  await db.query(
    `UPDATE users
     SET name = $1, phone = $2, occupation = $3, interests = $4
     WHERE id = $5`,
    [
      updates.name ?? existing.name,
      updates.phone ?? existing.phone,
      updates.occupation ?? existing.occupation,
      updates.interests ?? existing.interests,
      userId,
    ]
  );
  return findUserById(userId);
}

// Counts total joined, completed, and cancelled queues for the user profile screen.
export async function getUserQueueStats(userId: string) {
  const totalRes = await db.query<{ count: string }>(
    `SELECT COUNT(*) AS count FROM queue_entries WHERE user_id = $1`,
    [userId]
  );
  const completedRes = await db.query<{ count: string }>(
    `SELECT COUNT(*) AS count FROM queue_entries WHERE user_id = $1 AND status = 'SERVED'`,
    [userId]
  );
  const cancelledRes = await db.query<{ count: string }>(
    `SELECT COUNT(*) AS count FROM queue_entries WHERE user_id = $1 AND status = 'CANCELLED'`,
    [userId]
  );

  return {
    totalJoined: parseInt(totalRes.rows[0]?.count || '0', 10),
    completed: parseInt(completedRes.rows[0]?.count || '0', 10),
    cancelled: parseInt(cancelledRes.rows[0]?.count || '0', 10),
  };
}

// Returns all 12 campus services ordered alphabetically.
export async function getAllCampusServices() {
  const result = await db.query<any>(`SELECT * FROM services ORDER BY name ASC`);
  return result.rows.map(formatServiceRow);
}

// Returns a single campus service by its ID.
export async function getCampusServiceById(serviceId: string) {
  const result = await db.query<any>(`SELECT * FROM services WHERE id = $1 LIMIT 1`, [serviceId]);
  return result.rows[0] ? formatServiceRow(result.rows[0]) : null;
}

// Gets the user's currently active waiting ticket along with people ahead and estimated wait time.
export async function getActiveQueueEntryForUser(userId: string) {
  const res = await db.query<any>(
    `SELECT q.*, s.name AS s_name, s.category AS s_category, s.description AS s_desc,
            s.average_service_time AS s_avg_time, s.current_waiting_count AS s_wait_count, s.is_open AS s_open
     FROM queue_entries q
     JOIN services s ON q.service_id = s.id
     WHERE q.user_id = $1 AND q.status = 'WAITING'
     ORDER BY q.joined_at DESC
     LIMIT 1`,
    [userId]
  );

  if (!res.rows[0]) return null;
  const row = res.rows[0];

  await recalculateServiceWaitingCount(row.service_id);

  // Re-read updated position after recalculation.
  const updatedPosRes = await db.query<{ position: number }>(
    `SELECT position FROM queue_entries WHERE id = $1`,
    [row.id]
  );
  const position = updatedPosRes.rows[0]?.position || row.position;
  const peopleAhead = Math.max(0, position - 1);
  const estimatedWaitMinutes = peopleAhead * row.s_avg_time;

  return {
    queueEntry: formatQueueEntryWithService(row, position),
    peopleAhead,
    estimatedWaitMinutes,
  };
}

// Adds the user to the end of a service queue and calculates their position and wait time.
export async function joinCampusServiceQueue(userId: string, serviceId: string) {
  // Check if user is already waiting in a queue.
  const existingActive = await getActiveQueueEntryForUser(userId);
  if (existingActive) {
    throw new Error(`You are already in the queue for ${existingActive.queueEntry.service.name}`);
  }

  const service = await getCampusServiceById(serviceId);
  if (!service) {
    throw new Error('Campus service not found');
  }

  // Ensure at least 2 simulated people are ahead so user can test live queue movement.
  const waitingRes = await db.query<{ count: string }>(
    `SELECT COUNT(*) AS count FROM queue_entries WHERE service_id = $1 AND status = 'WAITING'`,
    [serviceId]
  );
  let waitingCount = parseInt(waitingRes.rows[0]?.count || '0', 10);

  if (waitingCount < 2) {
    const simUsers = await db.query<{ id: string }>(
      `SELECT id FROM users WHERE email LIKE '%@campus.sim' LIMIT 3`
    );
    for (let i = 0; i < Math.min(2, simUsers.rows.length); i++) {
      const pastTime = new Date(Date.now() - (3 - i) * 60000).toISOString();
      await db.query(
        `INSERT INTO queue_entries (id, user_id, service_id, position, status, joined_at)
         VALUES ($1, $2, $3, $4, 'WAITING', $5)`,
        [createId(), simUsers.rows[i].id, serviceId, i + 1, pastTime]
      );
    }
    await recalculateServiceWaitingCount(serviceId);
    const updatedRes = await db.query<{ count: string }>(
      `SELECT COUNT(*) AS count FROM queue_entries WHERE service_id = $1 AND status = 'WAITING'`,
      [serviceId]
    );
    waitingCount = parseInt(updatedRes.rows[0]?.count || '0', 10);
  }

  const newEntryId = createId();
  const newPosition = waitingCount + 1;

  await db.query(
    `INSERT INTO queue_entries (id, user_id, service_id, position, status, joined_at)
     VALUES ($1, $2, $3, $4, 'WAITING', $5)`,
    [newEntryId, userId, serviceId, newPosition, new Date().toISOString()]
  );

  await recalculateServiceWaitingCount(serviceId);
  return getActiveQueueEntryForUser(userId);
}

// Serves one simulated student ahead of the user so their queue position moves closer to #1.
export async function advanceQueueForUser(userId: string): Promise<void> {
  const active = await getActiveQueueEntryForUser(userId);
  if (!active) return;

  const serviceId = active.queueEntry.serviceId;
  const userPosition = active.queueEntry.position;

  // Find the first waiting entry ahead of this user in line.
  const aheadRes = await db.query<{ id: string }>(
    `SELECT id FROM queue_entries
     WHERE service_id = $1 AND status = 'WAITING' AND id != $2 AND position < $3
     ORDER BY position ASC, joined_at ASC
     LIMIT 1`,
    [serviceId, active.queueEntry.id, userPosition]
  );

  if (aheadRes.rows[0]) {
    await db.query(
      `UPDATE queue_entries SET status = 'SERVED', served_at = CURRENT_TIMESTAMP WHERE id = $1`,
      [aheadRes.rows[0].id]
    );
    await recalculateServiceWaitingCount(serviceId);
  }
}

// Marks the user's active queue ticket as SERVED (completed at the counter).
export async function completeQueueForUser(userId: string, queueId: string) {
  const res = await db.query<any>(
    `SELECT * FROM queue_entries WHERE id = $1 AND user_id = $2 LIMIT 1`,
    [queueId, userId]
  );
  if (!res.rows[0]) {
    throw new Error('Queue ticket not found');
  }

  await db.query(
    `UPDATE queue_entries SET status = 'SERVED', served_at = CURRENT_TIMESTAMP WHERE id = $1`,
    [queueId]
  );
  await recalculateServiceWaitingCount(res.rows[0].service_id);

  const service = await getCampusServiceById(res.rows[0].service_id);
  return {
    id: queueId,
    userId,
    serviceId: res.rows[0].service_id,
    position: res.rows[0].position,
    status: 'SERVED',
    joinedAt: res.rows[0].joined_at,
    servedAt: new Date().toISOString(),
    service,
  };
}

// Cancels the user's active queue ticket when they leave the line.
export async function leaveQueueForUser(userId: string, queueId: string) {
  const res = await db.query<any>(
    `SELECT * FROM queue_entries WHERE id = $1 AND user_id = $2 LIMIT 1`,
    [queueId, userId]
  );
  if (!res.rows[0]) {
    throw new Error('Queue ticket not found');
  }

  await db.query(
    `UPDATE queue_entries SET status = 'CANCELLED', cancelled_at = CURRENT_TIMESTAMP WHERE id = $1`,
    [queueId]
  );
  await recalculateServiceWaitingCount(res.rows[0].service_id);

  const service = await getCampusServiceById(res.rows[0].service_id);
  return {
    id: queueId,
    userId,
    serviceId: res.rows[0].service_id,
    position: res.rows[0].position,
    status: 'CANCELLED',
    joinedAt: res.rows[0].joined_at,
    cancelledAt: new Date().toISOString(),
    service,
  };
}

// Returns all past and active queue tickets for the logged-in user.
export async function getQueueHistoryForUser(userId: string) {
  const res = await db.query<any>(
    `SELECT q.*, s.name AS s_name, s.category AS s_category, s.description AS s_desc,
            s.average_service_time AS s_avg_time, s.current_waiting_count AS s_wait_count, s.is_open AS s_open
     FROM queue_entries q
     JOIN services s ON q.service_id = s.id
     WHERE q.user_id = $1
     ORDER BY q.joined_at DESC`,
    [userId]
  );

  return res.rows.map((row) => formatQueueEntryWithService(row, row.position));
}

// Converts a snake_case SQL service row into a camelCase JSON object for the Android app.
function formatServiceRow(row: any) {
  return {
    id: row.id,
    name: row.name,
    category: row.category || 'General',
    description: row.description,
    averageServiceTime: Number(row.average_service_time),
    currentWaitingCount: Number(row.current_waiting_count),
    isOpen: Boolean(row.is_open),
  };
}

// Converts a joined SQL queue_entries + services row into the nested JSON object expected by Android.
function formatQueueEntryWithService(row: any, position: number) {
  return {
    id: row.id,
    userId: row.user_id,
    serviceId: row.service_id,
    position: Number(position),
    status: row.status,
    joinedAt: row.joined_at,
    servedAt: row.served_at || null,
    cancelledAt: row.cancelled_at || null,
    service: {
      id: row.service_id,
      name: row.s_name,
      category: row.s_category || 'General',
      description: row.s_desc,
      averageServiceTime: Number(row.s_avg_time),
      currentWaitingCount: Number(row.s_wait_count),
      isOpen: Boolean(row.s_open),
    },
  };
}
