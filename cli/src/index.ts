import { Command } from "commander";
import { registerInit } from "./commands/init.js";
import { registerConfigInfo } from "./commands/config-info.js";
import { registerProject } from "./commands/project.js";
import { registerLocale } from "./commands/locale.js";
import { registerKey } from "./commands/key.js";
import { registerUser } from "./commands/user.js";
import { registerPull } from "./commands/pull.js";
import { registerPush } from "./commands/push.js";
import { eprint } from "./print.js";
import type { GlobalOptions } from "./config.js";

const program = new Command();

program
  .name("translatr")
  .description("Command line interface for Translatr")
  .option("-e, --endpoint <url>", "the URL to the Translatr endpoint (default: from .translatr.yml)")
  .option("-t, --access-token <token>", "the access token to be used (default: from .translatr.yml)")
  .option("--default-locale <name>", "the default locale name for the target");

function getGlobal(): GlobalOptions {
  const opts = program.opts();
  return {
    endpoint: opts.endpoint,
    accessToken: opts.accessToken,
    defaultLocale: opts.defaultLocale,
  };
}

registerInit(program);
registerConfigInfo(program);
registerProject(program, getGlobal);
registerLocale(program, getGlobal);
registerKey(program, getGlobal);
registerUser(program, getGlobal);
registerPull(program, getGlobal);
registerPush(program, getGlobal);

program
  .parseAsync(process.argv)
  .catch((e) => {
    eprint(e instanceof Error ? e.message : String(e));
    process.exitCode = 1;
  });
