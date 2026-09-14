import type { Command } from "commander";
import { readFileSync } from "node:fs";
import { glob } from "glob";
import { Api, type Locale } from "../api.js";
import { assertExists, readConfigMerge, type TranslatrConfig } from "../config.js";
import type { GlobalOptions } from "../config.js";
import { eprint } from "../print.js";

function targetPattern(target: string): RegExp {
  const regex = target.replace(/\{[^}]*\}/g, (m) =>
    `(?<${m.slice(1, -1).replace(/\./g, "_")}>.*)`,
  );
  return new RegExp(`^${regex}$`);
}

function targetFilter(target: string): string {
  // mirrors the Python CLI's re.sub(r'(.\?)?\{locale.name\}', r'*', target):
  // the optional group is "any char immediately followed by a literal '?'",
  // not "any single optional char" - e.g. for "i18n/{locale.name}.json" (no
  // literal "?" before the brace) nothing before "{locale.name}" is consumed.
  return target.replace(/(.\?)?\{locale\.name\}/g, "*");
}

export function registerPush(program: Command, getGlobal: () => GlobalOptions): void {
  program
    .command("push")
    .description(
      "pushing sends matching messages files to the given endpoint, creating locales if needed",
    )
    .action(async () => {
      const config = readConfigMerge(getGlobal());
      assertExists(
        config as unknown as Record<string, unknown>,
        "push.target",
        "push.file_type",
        "default_locale",
      );

      const api = new Api(config);
      const localesByName = new Map<string, Locale>((await api.locales()).map((l) => [l.name, l]));

      const target = config.push.target;
      const filter = targetFilter(target);
      const pattern = targetPattern(target);

      const filenames = await glob(filter);
      for (const filename of filenames) {
        const match = pattern.exec(filename);
        if (!match) {
          console.log(`Filename ${filename} does not match target: ${target}`);
          continue;
        }
        const localeName = match.groups?.locale_name || config.default_locale;

        let created = false;
        if (!localesByName.has(localeName)) {
          try {
            localesByName.set(localeName, await api.localeCreate(localeName));
            created = true;
          } catch (e) {
            eprint(String(e instanceof Error ? e.message : e));
          }
        }

        const locale = localesByName.get(localeName);
        if (locale) {
          try {
            await api.localeImport(locale.id, config.push.file_type, readFileSync(filename), filename);
            console.log(`Uploaded ${filename} to ${localeName}${created ? " (new)" : ""}`);
          } catch (e) {
            eprint(String(e instanceof Error ? e.message : e));
          }
        } else {
          console.log(`Could neither find nor create locale ${localeName}`);
        }
      }
    });
}
