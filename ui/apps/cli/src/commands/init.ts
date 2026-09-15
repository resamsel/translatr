import type { Command } from "commander";
import { writeInitConfig, type InitTarget } from "../config.js";

const DEFAULT_TARGET: InitTarget = {
  target: "conf/messages.?{locale.name}",
  fileType: "play_messages",
};

function parseTarget(value: string, previous: InitTarget[]): InitTarget[] {
  const idx = value.lastIndexOf(":");
  if (idx <= 0 || idx === value.length - 1) {
    throw new Error(
      `Invalid --target "${value}": expected "<path>:<file_type>" (e.g. "i18n/{locale.name}.json:json")`,
    );
  }
  const target = value.slice(0, idx);
  const fileType = value.slice(idx + 1);
  return [...previous, { target, fileType }];
}

export function registerInit(program: Command): void {
  program
    .command("init")
    .description("initialises the directory with a .translatr.yml file")
    .argument("<endpoint>", "the URL to the Translatr endpoint")
    .argument("<access_token>", "the access token for API calls")
    .argument("<project_id>", "the ID of the Translatr project")
    .option(
      "--target <path>:<file_type>",
      "a target location and its file format; repeat for multiple targets",
      parseTarget,
      [] as InitTarget[],
    )
    .action((endpoint: string, accessToken: string, projectId: string, opts) => {
      const targets: InitTarget[] = opts.target.length > 0 ? opts.target : [DEFAULT_TARGET];
      writeInitConfig({ endpoint, accessToken, projectId, targets });
      console.log("Config initialised into .translatr.yml");
    });
}
