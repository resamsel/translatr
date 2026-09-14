import type { Command } from "commander";
import { Api, isUuid } from "../api.js";
import { readConfigMerge } from "../config.js";
import { printTable } from "../print.js";
import type { GlobalOptions } from "../config.js";

export function registerKey(program: Command, getGlobal: () => GlobalOptions): void {
  const key = program.command("key").description("key commands");

  key
    .command("ls")
    .description("list keys")
    .argument("[search]", "the search string")
    .option("-p, --project-id <id>", "the project ID")
    .action(async (search: string | undefined, opts) => {
      const config = readConfigMerge({ ...getGlobal(), projectId: opts.projectId });
      const keys = await new Api(config).keys(search);
      printTable(keys.map((k) => [k.id, k.name]));
    });

  key
    .command("create")
    .description("create key")
    .argument("<key_name>", "the key name")
    .option("-p, --project-id <id>", "the project ID")
    .action(async (keyName: string, opts) => {
      const config = readConfigMerge({ ...getGlobal(), projectId: opts.projectId });
      const k = await new Api(config).keyCreate(keyName);
      console.log(`Key ${k.name} has been created`);
    });

  key
    .command("rm")
    .description("remove keys")
    .argument("<key_ids...>", "the key IDs (or names)")
    .option("-p, --project-id <id>", "the project ID")
    .action(async (keyIds: string[], opts) => {
      const config = readConfigMerge({ ...getGlobal(), projectId: opts.projectId });
      const api = new Api(config);
      for (let keyId of keyIds) {
        if (!isUuid(keyId)) {
          const matches = (await api.keys(keyId)).filter((k) => k.name === keyId);
          if (matches.length === 0) {
            throw new Error(`Key with ID '${keyId}' not found`);
          }
          keyId = matches[0].id;
        }
        const k = await api.keyDelete(keyId);
        console.log(`Key ${k.name} has been deleted`);
      }
    });
}
