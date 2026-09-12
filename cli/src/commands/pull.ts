import type { Command } from "commander";
import { Api, type Locale } from "../api.js";
import { assertExists, readConfigMerge, type TranslatrConfig } from "../config.js";
import type { GlobalOptions } from "../config.js";

function pullTargetFromLocale(config: TranslatrConfig, locale: Locale): string {
  const defaultLocaleName = config.default_locale;
  const pullTarget = config.pull.target;
  const isDefaultLocale = locale.name === defaultLocaleName;

  let name = locale.name;
  if (isDefaultLocale && !/\.\?/.test(pullTarget)) {
    name = defaultLocaleName;
  }

  let target = pullTarget;
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
      assertExists(
        config as unknown as Record<string, unknown>,
        "pull.target",
        "pull.file_type",
        "default_locale",
      );

      const api = new Api(config);
      for (const locale of await api.locales()) {
        const target = pullTargetFromLocale(config, locale);
        await api.localeExportToFile(locale.id, config.pull.file_type, target);
        console.log(`Downloaded ${locale.name} to ${target}`);
      }
    });
}
