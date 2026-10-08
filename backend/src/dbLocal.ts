// FILE TYPE: Embedded Local PostgreSQL Engine (PGlite Socket Runner)
// PURPOSE: Runs embedded WASM PostgreSQL server listening on 127.0.0.1:5432 over true Postgres wire protocol.
// STORES DATA IN: backend/.pglite-data/

import path from 'path';
import { PGlite } from '@electric-sql/pglite';
import { PGLiteSocketServer } from '@electric-sql/pglite-socket';

const dbDataDir = path.resolve(process.cwd(), '.pglite-data');

const schemaDDL = `
DO $$ BEGIN
  CREATE TYPE "Role" AS ENUM ('USER', 'ADMIN');
EXCEPTION
  WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
  CREATE TYPE "QueueStatus" AS ENUM ('WAITING', 'SERVED', 'CANCELLED');
EXCEPTION
  WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS "User" (
  "id" TEXT PRIMARY KEY,
  "name" TEXT NOT NULL,
  "email" TEXT UNIQUE NOT NULL,
  "passwordHash" TEXT NOT NULL,
  "occupation" TEXT,
  "interests" TEXT,
  "role" "Role" NOT NULL DEFAULT 'USER',
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "lastActiveAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "Service" (
  "id" TEXT PRIMARY KEY,
  "name" TEXT NOT NULL,
  "description" TEXT NOT NULL,
  "averageServiceTime" INTEGER NOT NULL,
  "currentWaitingCount" INTEGER NOT NULL DEFAULT 0,
  "isOpen" BOOLEAN NOT NULL DEFAULT true,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "QueueEntry" (
  "id" TEXT PRIMARY KEY,
  "userId" TEXT NOT NULL REFERENCES "User"("id") ON DELETE CASCADE,
  "serviceId" TEXT NOT NULL REFERENCES "Service"("id") ON DELETE CASCADE,
  "position" INTEGER NOT NULL,
  "status" "QueueStatus" NOT NULL DEFAULT 'WAITING',
  "joinedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "servedAt" TIMESTAMP(3),
  "cancelledAt" TIMESTAMP(3)
);

CREATE TABLE IF NOT EXISTS "AuditLog" (
  "id" TEXT PRIMARY KEY,
  "userId" TEXT NOT NULL REFERENCES "User"("id") ON DELETE CASCADE,
  "action" TEXT NOT NULL,
  "entity" TEXT NOT NULL,
  "entityId" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);
`;

let serverInstance: PGLiteSocketServer | null = null;
let startingPromise: Promise<PGLiteSocketServer | null> | null = null;

export async function startLocalPostgres(): Promise<PGLiteSocketServer | null> {
  if (serverInstance) return serverInstance;
  if (startingPromise) return startingPromise;

  startingPromise = (async () => {
    try {
      const db = await PGlite.create(dbDataDir);
      
      // Auto-ensure PostgreSQL schema and native enums exist
      await db.exec(schemaDDL);

      const server = new PGLiteSocketServer({
        db,
        port: 5432,
        host: '127.0.0.1',
        maxConnections: 20,
      });

      await server.start();
      serverInstance = server;

      console.log(`🚀 Embedded PostgreSQL Engine (PGlite) running on 127.0.0.1:5432`);
      console.log(`📁 Local Data Directory: ${dbDataDir}`);
      console.log(`🔗 Connection URL: postgresql://postgres:postgres@127.0.0.1:5432/postgres?schema=public`);

      return serverInstance;
    } catch (err: any) {
      if (err.code === 'EADDRINUSE') {
        console.log('⚡ PostgreSQL instance is already running on port 5432 (shared instance active).');
      } else {
        console.error('Failed to start embedded PostgreSQL server:', err.message || err);
      }
      return null;
    } finally {
      startingPromise = null;
    }
  })();

  return startingPromise;
}

export async function stopLocalPostgres(): Promise<void> {
  if (serverInstance) {
    await serverInstance.stop();
    serverInstance = null;
  }
}

// Auto-start when imported or executed directly
startLocalPostgres();
