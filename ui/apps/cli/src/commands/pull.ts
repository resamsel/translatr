import type { Command } from "commander";
import { Api, type Locale } from "../api.js";
import { assertExists, assertTargetsNonEmpty, readConfigMerge } from "../config.js";
import type { GlobalOptions } from "../config.js";

function pullTargetFromLocale(targetPattern: string, defaultLocaleName: string, locale: Locale): string {
  const isDefaultLocale = locale.name === defaultLocaleName;

  let name = locale.name;
  if (isDefaultLocale && !/\.\?/.test(targetPattern)) {
    name = defaultLocaleName;
  }

  let target = targetPattern;
  if (isDefaultLocale) {
    // the .?{locale.name} part of the target isn't needed for the default locale
    target = target.replace(/.\?\{locale\.name\}/, "");
  }

  target = target.replace("?{locale.name}", "{locale.name}").replace(/\{locale\.name\}/g, name);
  return target;
}

export function registerPull(program: Command, getGlobal: () => GlobalOptions): void {
  program
    .command("pull")
    .description("pulling downloads all locales into configured locations")
    .action(async () => {
      const config = readConfigMerge(getGlobal());
      assertExists(config as unknown as Record<string, unknown>, "targets", "default_locale");
      assertTargetsNonEmpty(config);

      const api = new Api(config);
      const locales = await api.locales();
      for (const [targetPattern, spec] of Object.entries(config.targets)) {
        for (const locale of locales) {
          const target = pullTargetFromLocale(targetPattern, config.default_locale, locale);
          await api.localeExportToFile(locale.id, spec.file_type, target);
          console.log(`Downloaded ${locale.name} to ${target}`);
        }
      }
    });
}
