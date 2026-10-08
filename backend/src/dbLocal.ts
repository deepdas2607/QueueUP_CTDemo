// FILE TYPE: Embedded Local PostgreSQL Engine (PGlite Socket Runner)
// PURPOSE: Runs embedded WASM PostgreSQL server listening on 127.0.0.1:5432 over true Postgres wire protocol.
// STORES DATA IN: backend/.pglite-data/

import net from 'net';
import { PGlite } from '@electric-sql/pglite';
import { PostgresConnection } from 'pg-gateway';

const dbDataDir = './.pglite-data';
const db = new PGlite(dbDataDir);

const server = net.createServer((socket) => {
  new PostgresConnection(socket as any, {
    auth: { method: 'trust' },
    async onQuery(query: string) {
      try {
        const res = await db.query(query);
        return {
          rows: res.rows ? res.rows.map((r: any) => Object.values(r)) : [],
          columns: res.fields ? res.fields.map((f: any) => ({ name: f.name, type: f.dataTypeID })) : [],
        };
      } catch (err: any) {
        console.error('PGlite Query Error:', err);
        throw err;
      }
    },
  } as any);
});

const PORT = 5432;
const HOST = '127.0.0.1';

server.listen(PORT, HOST, () => {
  console.log(`🚀 Embedded PostgreSQL Engine (PGlite) running on ${HOST}:${PORT}`);
  console.log(`📁 Local Data Directory: ${dbDataDir}`);
  console.log(`🔗 Connection URL: postgresql://postgres:postgres@127.0.0.1:5432/queueup_db?schema=public`);
});
