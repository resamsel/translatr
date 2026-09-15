import { describe, expect, it } from "bun:test";
import { writeFileSync } from "node:fs";
import { Command } from "commander";
import yaml from "js-yaml";
import { registerConfigInfo } from "./config-info.js";
import { withTempCwd } from "../test-utils.js";

const CONFIG_MULTI_TARGET = [
  "translatr:",
  "  endpoint: http://localhost:9000",
  "  access_token: tok",
  "  project_id: proj-1",
  "  default_locale: default",
  "  targets:",
  "    app1/messages.?{locale.name}:",
  "      file_type: json",
  "    app2/messages.?{locale.name}:",
  "      file_type: json",
].join("\n");

describe("`config` command reflects the `targets` map", () => {
  it("Config dump shows all targets", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG_MULTI_TARGET);

      const logs: string[] = [];
      const originalLog = console.log;
      console.log = (...args: unknown[]) => logs.push(args.join(" "));
      try {
        const program = new Command();
        program.exitOverride();
        registerConfigInfo(program);
        await program.parseAsync(["config"], { from: "user" });
      } finally {
        console.log = originalLog;
      }

      const printed = yaml.load(logs.join("\n")) as {
        translatr: { targets: Record<string, { file_type: string }> };
      };

      expect(printed.translatr.targets).toEqual({
        "app1/messages.?{locale.name}": { file_type: "json" },
        "app2/messages.?{locale.name}": { file_type: "json" },
      });
    });
  });
});

describe("`config` command prints the flat shape", () => {
  it("Config dump has no wrapper: top-level keys, not a single `translatr:` key", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG_MULTI_TARGET);

      const logs: string[] = [];
      const originalLog = console.log;
      console.log = (...args: unknown[]) => logs.push(args.join(" "));
      try {
        const program = new Command();
        program.exitOverride();
        registerConfigInfo(program);
        await program.parseAsync(["config"], { from: "user" });
      } finally {
        console.log = originalLog;
      }

      const printed = yaml.load(logs.join("\n")) as Record<string, unknown>;

      expect(printed.translatr).toBeUndefined();
      expect(printed.targets).toEqual({
        "app1/messages.?{locale.name}": { file_type: "json" },
        "app2/messages.?{locale.name}": { file_type: "json" },
      });
    });
  });
});
