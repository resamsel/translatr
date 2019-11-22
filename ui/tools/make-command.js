#!/usr/bin/env node

var fs = require('fs');
var filename = process.argv[2];
var target = process.argv[3];
var data = '#!/usr/bin/env node\n\n' + fs.readFileSync(filename);
var handler = err => {
  if (err) {
    console.error(err);
  }
};

fs.writeFile(target, data, handler);
fs.chmod(target, 0o755, handler);
