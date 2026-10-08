// FILE TYPE: Server Launcher Script
// PURPOSE: Starts local PGlite PostgreSQL engine and Express REST API server in a single node process.

require('./dist/dbLocal.js');

setTimeout(() => {
  require('./dist/server.js');
}, 1500);
