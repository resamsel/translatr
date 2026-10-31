// Stages both production SPA bundles into the single static root Quinoa serves
// (public/app/{ui,admin}). The Angular `application` builder writes each app to
// <output-path>/browser/, so flatten that level away: /ui/** and /admin/** map 1:1
// onto public/app/ui and public/app/admin.
const { resolve } = require('path');
const { copySync, emptyDirSync } = require('fs-extra');

const publicDir = resolve(__dirname, '..', '..', 'public');
const target = resolve(publicDir, 'app');

emptyDirSync(target);
for (const app of ['ui', 'admin']) {
  copySync(resolve(publicDir, app, 'browser'), resolve(target, app));
}
