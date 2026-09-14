import { describe, expect, it } from "bun:test";
import { readFileSync } from "node:fs";
import { Command } from "commander";
import yaml from "js-yaml";
import { registerInit } from "./init.js";
import { withTempCwd } from "../test-utils.js";

describe("`init` scaffolds the config file", () => {
  it("Successful init: writes .translatr.yml with given values and defaults, and prints a confirmation", async () => {
    await withTempCwd(async () => {
      const logs: string[] = [];
      const originalLog = console.log;
      console.log = (...args: unknown[]) => logs.push(args.join(" "));

      try {
        const program = new Command();
        program.exitOverride();
        registerInit(program);
        await program.parseAsync(
          ["init", "https://translatr.example", "abc123", "my-project-id"],
          { from: "user" },
        );
      } finally {
        console.log = originalLog;
      }

      const written = yaml.load(readFileSync(".translatr.yml", "utf8")) as {
        translatr: {
          endpoint: string;
          access_token: string;
          project_id: string;
          push: { file_type: string; target: string };
          pull: { file_type: string; target: string };
        };
      };

      expect(written.translatr.endpoint).toBe("https://translatr.example");
      expect(written.translatr.access_token).toBe("abc123");
      expect(written.translatr.project_id).toBe("my-project-id");
      expect(written.translatr.push).toEqual({
        file_type: "play_messages",
        target: "conf/messages.?{locale.name}",
      });
      expect(written.translatr.pull).toEqual({
        file_type: "play_messages",
        target: "conf/messages.?{locale.name}",
      });
      expect(logs.some((l) => l.toLowerCase().includes("initialised"))).toBe(true);
    });
  });
});
