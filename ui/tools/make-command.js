#!/usr/bin/env node

var fs = require('fs');
var filename = process.argv[2];
var target = process.argv[3];
var handler = err => {
  if (err) {
    console.error(err);
  }
};
var data = '#!/usr/bin/env node\n\n' + fs.readFileSync(filename);

fs.writeFile(
  target,
  data,
  { encoding: 'utf8', mode: 0o755, flag: 'w' },
  handler
);
