import type { Command } from "commander";
import { Api } from "../api.js";
import { readConfigMerge } from "../config.js";
import { printTable } from "../print.js";
import type { GlobalOptions } from "../config.js";

export function registerUser(program: Command, getGlobal: () => GlobalOptions): void {
  const user = program.command("user").description("user commands");

  user
    .command("ls")
    .description("list users")
    .argument("[search]", "the search string")
    .action(async (search?: string) => {
      const config = readConfigMerge(getGlobal());
      const users = await new Api(config).users(search);
      printTable(users.map((u) => [u.id, u.name, u.username]));
    });
}
