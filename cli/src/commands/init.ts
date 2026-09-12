import type { Command } from "commander";
import { writeInitConfig } from "../config.js";

export function registerInit(program: Command): void {
  program
    .command("init")
    .description("initialises the directory with a .translatr.yml file")
    .argument("<endpoint>", "the URL to the Translatr endpoint")
    .argument("<access_token>", "the access token for API calls")
    .argument("<project_id>", "the ID of the Translatr project")
    .option("--pull-file-type <type>", "format of files to be downloaded", "play_messages")
    .option("--pull-target <target>", "location format of downloaded files", "conf/messages.?{locale.name}")
    .option("--push-file-type <type>", "format of files to be uploaded", "play_messages")
    .option("--push-target <target>", "location format of uploaded files", "conf/messages.?{locale.name}")
    .action((endpoint: string, accessToken: string, projectId: string, opts) => {
      writeInitConfig({
        endpoint,
        accessToken,
        projectId,
        pullFileType: opts.pullFileType,
        pullTarget: opts.pullTarget,
        pushFileType: opts.pushFileType,
        pushTarget: opts.pushTarget,
      });
      console.log("Config initialised into .translatr.yml");
    });
}
