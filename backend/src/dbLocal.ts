// FILE TYPE: Embedded Local PostgreSQL Engine (PGlite Socket Runner)
// PURPOSE: Runs embedded WASM PostgreSQL server listening on 127.0.0.1:5432 over true Postgres wire protocol.
// STORES DATA IN: backend/.pglite-data/

import net from 'net';
import fs from 'fs';
import path from 'path';
import { Readable, Writable } from 'stream';
import { PGlite } from '@electric-sql/pglite';
import { PostgresConnection } from 'pg-gateway';

const dbDataDir = path.resolve(process.cwd(), '.pglite-data');

// Ensure data directory exists
if (!fs.existsSync(dbDataDir)) {
  fs.mkdirSync(dbDataDir, { recursive: true });
}

// Clean up stale lock/pid file if previous process was forcefully terminated
const stalePidFile = path.join(dbDataDir, 'postmaster.pid');
if (fs.existsSync(stalePidFile)) {
  try {
    fs.unlinkSync(stalePidFile);
  } catch {
    // Ignore cleanup errors
  }
}

const db = new PGlite(dbDataDir);

const server = net.createServer((socket) => {
  socket.on('error', () => {});

  try {
    const duplex = {
      readable: Readable.toWeb(socket),
      writable: Writable.toWeb(socket),
    };

    new PostgresConnection(duplex as any, {
      auth: { method: 'trust' },
      serverVersion: '16.0',
      async onQuery(query: string) {
        try {
          const res = await db.query(query);
          return {
            rows: res.rows ? res.rows.map((r: any) => Object.values(r)) : [],
            columns: res.fields ? res.fields.map((f: any) => ({ name: f.name, type: f.dataTypeID })) : [],
          };
        } catch {
          return { rows: [], columns: [] };
        }
      },
    } as any);
  } catch {
    // Ignore teardowns
  }
});

server.on('error', (err: any) => {
  if (err.code === 'EADDRINUSE') {
    console.log('⚡ PostgreSQL instance is already running on port 5432 (shared instance in use).');
  } else {
    console.error('PostgreSQL server error:', err.message || err);
  }
});

process.on('uncaughtException', () => {});

const PORT = 5432;
const HOST = '127.0.0.1';

server.listen(PORT, HOST, () => {
  console.log(`🚀 Embedded PostgreSQL Engine (PGlite) running on ${HOST}:${PORT}`);
  console.log(`📁 Local Data Directory: ${dbDataDir}`);
  console.log(`🔗 Connection URL: postgresql://postgres:postgres@127.0.0.1:5432/queueup_db?schema=public`);
});
