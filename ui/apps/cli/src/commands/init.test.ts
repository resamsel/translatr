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
          targets: Record<string, { file_type: string }>;
        };
      };

      expect(written.translatr.endpoint).toBe("https://translatr.example");
      expect(written.translatr.access_token).toBe("abc123");
      expect(written.translatr.project_id).toBe("my-project-id");
      expect(written.translatr.targets).toEqual({
        "conf/messages.?{locale.name}": { file_type: "play_messages" },
      });
      expect(logs.some((l) => l.toLowerCase().includes("initialised"))).toBe(true);
    });
  });
});

describe("`init` seeds one or more targets", () => {
  it("Single target via init: writes a `targets` map with exactly that one entry", async () => {
    await withTempCwd(async () => {
      const program = new Command();
      program.exitOverride();
      registerInit(program);
      await program.parseAsync(
        [
          "init",
          "https://translatr.example",
          "abc123",
          "my-project-id",
          "--target",
          "conf/messages.?{locale.name}:play_messages",
        ],
        { from: "user" },
      );

      const written = yaml.load(readFileSync(".translatr.yml", "utf8")) as {
        translatr: { targets: Record<string, { file_type: string }> };
      };

      expect(written.translatr.targets).toEqual({
        "conf/messages.?{locale.name}": { file_type: "play_messages" },
      });
    });
  });

  it("Repeated --target flags: writes a `targets` map with every entry", async () => {
    await withTempCwd(async () => {
      const program = new Command();
      program.exitOverride();
      registerInit(program);
      await program.parseAsync(
        [
          "init",
          "https://translatr.example",
          "abc123",
          "my-project-id",
          "--target",
          "app1/{locale.name}.json:json",
          "--target",
          "app2/{locale.name}.json:json",
        ],
        { from: "user" },
      );

      const written = yaml.load(readFileSync(".translatr.yml", "utf8")) as {
        translatr: { targets: Record<string, { file_type: string }> };
      };

      expect(written.translatr.targets).toEqual({
        "app1/{locale.name}.json": { file_type: "json" },
        "app2/{locale.name}.json": { file_type: "json" },
      });
    });
  });

  it("Malformed --target token: rejects with a clear error instead of writing a bad config", async () => {
    await withTempCwd(async () => {
      const program = new Command();
      program.exitOverride();
      registerInit(program);

      await expect(
        program.parseAsync(
          ["init", "https://translatr.example", "abc123", "my-project-id", "--target", "no-colon-here"],
          { from: "user" },
        ),
      ).rejects.toThrow(/--target/);
    });
  });
});

describe("`init` writes the flat shape", () => {
  it("Init output has no wrapper: endpoint/targets etc are top-level keys", async () => {
    await withTempCwd(async () => {
      const program = new Command();
      program.exitOverride();
      registerInit(program);
      await program.parseAsync(
        [
          "init",
          "https://translatr.example",
          "abc123",
          "my-project-id",
          "--target",
          "conf/messages.?{locale.name}:play_messages",
        ],
        { from: "user" },
      );

      const written = yaml.load(readFileSync(".translatr.yml", "utf8")) as Record<string, unknown>;

      expect(written.translatr).toBeUndefined();
      expect(written.endpoint).toBe("https://translatr.example");
      expect(written.targets).toEqual({
        "conf/messages.?{locale.name}": { file_type: "play_messages" },
      });
    });
  });
});
