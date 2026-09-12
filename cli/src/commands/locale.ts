import type { Command } from "commander";
import { Api, isUuid } from "../api.js";
import { readConfigMerge } from "../config.js";
import { printTable } from "../print.js";
import type { GlobalOptions } from "../config.js";

export function registerLocale(program: Command, getGlobal: () => GlobalOptions): void {
  const locale = program.command("locale").description("locale commands");

  locale
    .command("ls")
    .description("list locales")
    .argument("[search]", "the search string")
    .option("-p, --project-id <id>", "the project ID")
    .action(async (search: string | undefined, opts) => {
      const config = readConfigMerge({ ...getGlobal(), projectId: opts.projectId });
      const locales = await new Api(config).locales(search);
      printTable(locales.map((l) => [l.id, l.name]));
    });

  locale
    .command("create")
    .description("create locale")
    .argument("<locale_name>", "the locale name")
    .option("-p, --project-id <id>", "the project ID")
    .action(async (localeName: string, opts) => {
      const config = readConfigMerge({ ...getGlobal(), projectId: opts.projectId });
      const l = await new Api(config).localeCreate(localeName);
      console.log(`Locale ${l.name} has been created`);
    });

  locale
    .command("rm")
    .description("remove locales")
    .argument("<locale_ids...>", "the locale IDs (or names)")
    .option("-p, --project-id <id>", "the project ID")
    .action(async (localeIds: string[], opts) => {
      const config = readConfigMerge({ ...getGlobal(), projectId: opts.projectId });
      const api = new Api(config);
      for (let localeId of localeIds) {
        if (!isUuid(localeId)) {
          const matches = (await api.locales(localeId)).filter((l) => l.name === localeId);
          if (matches.length === 0) {
            throw new Error(`Locale with ID '${localeId}' not found`);
          }
          localeId = matches[0].id;
        }
        const l = await api.localeDelete(localeId);
        console.log(`Locale ${l.name} has been deleted`);
      }
    });
}
