const { version, name, author, bugs, homepage } = require('../package.json');
const { resolve, relative } = require('path');
const { writeFileSync } = require('fs-extra');
const dateFormat = require('dateformat');

const now = new Date();

const info = {
  name,
  version,
  author,
  homepage,
  bugsUrl: bugs.url,
  buildDate: now,
  buildVersion: `${version}-${dateFormat('yyyymmddhhMMss')}`,
  copyrightYear: now.getFullYear()
};

const file = resolve(__dirname, '..', 'libs', 'translatr-model', 'src', 'build-info.ts');
writeFileSync(
  file,
  `// IMPORTANT: THIS FILE IS AUTO GENERATED! DO NOT MANUALLY EDIT OR CHECKIN!
/* tslint:disable */
export interface BuildInfo {
  name: string;
  version: string;
  author: string;
  homepage: string;
  bugsUrl: string;
  buildDate: string;
  buildVersion: string;
  copyrightYear: number;
}
export const BUILD_INFO: BuildInfo = ${JSON.stringify(info, null, 2)};
/* tslint:enable */
`,
  { encoding: 'utf-8' }
);

console.log(`Wrote build info ${info.version} to ${relative(resolve(__dirname, '..'), file)}`);
