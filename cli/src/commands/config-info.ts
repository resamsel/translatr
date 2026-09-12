import type { Command } from "commander";
import yaml from "js-yaml";
import { readConfig } from "../config.js";

export function registerConfigInfo(program: Command): void {
  program
    .command("config")
    .description("show info about configuration")
    .action(() => {
      console.log(yaml.dump({ translatr: readConfig() }));
    });
}
