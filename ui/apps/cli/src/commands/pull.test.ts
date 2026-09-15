import { describe, expect, it } from "bun:test";
import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { Command } from "commander";
import { registerPull } from "./pull.js";
import { withTempCwd, mockFetch } from "../test-utils.js";

const CONFIG = [
  "endpoint: http://localhost:9000",
  "access_token: tok",
  "project_id: proj-1",
  "default_locale: default",
  "targets:",
  "  conf/messages.?{locale.name}:",
  "    file_type: json",
].join("\n");

async function runPull(locales: { id: string; name: string }[], exportBody: string) {
  const { fetchFn } = mockFetch(({ url, method }) => {
    if (method === "GET" && url.includes("/api/project/proj-1/locales")) {
      return { json: { list: locales } };
    }
    if (method === "GET" && url.includes("/export/")) {
      return { text: exportBody };
    }
    throw new Error(`Unexpected request: ${method} ${url}`);
  });
  const originalFetch = globalThis.fetch;
  globalThis.fetch = fetchFn;
  try {
    const program = new Command();
    program.exitOverride();
    registerPull(program, () => ({}));
    await program.parseAsync(["pull"], { from: "user" });
  } finally {
    globalThis.fetch = originalFetch;
  }
}

describe("`pull` downloads every locale to its configured target", () => {
  it("Pulling the default locale omits the locale suffix", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG);
      await runPull([{ id: "loc-default", name: "default" }], "DEFAULT_CONTENT");

      expect(existsSync("conf/messages")).toBe(true);
      expect(readFileSync("conf/messages", "utf8")).toBe("DEFAULT_CONTENT");
      expect(existsSync("conf/messages.default")).toBe(false);
    });
  });

  it("Pulling a non-default locale includes the locale suffix", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG);
      await runPull([{ id: "loc-de", name: "de" }], "DE_CONTENT");

      expect(existsSync("conf/messages.de")).toBe(true);
      expect(readFileSync("conf/messages.de", "utf8")).toBe("DE_CONTENT");
    });
  });
});

const CONFIG_MULTI_TARGET = [
  "endpoint: http://localhost:9000",
  "access_token: tok",
  "project_id: proj-1",
  "default_locale: default",
  "targets:",
  "  app1/messages.?{locale.name}:",
  "    file_type: json",
  "  app2/messages.?{locale.name}:",
  "    file_type: json",
].join("\n");

const LEGACY_CONFIG = [
  "endpoint: http://localhost:9000",
  "access_token: tok",
  "project_id: proj-1",
  "default_locale: default",
  "pull:",
  "  file_type: json",
  "  target: conf/messages.?{locale.name}",
].join("\n");

describe("Config declares a `targets` map instead of single `pull`/`push` targets", () => {
  it("Multiple targets: pull writes every configured target for every locale", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG_MULTI_TARGET);
      await runPull(
        [
          { id: "loc-default", name: "default" },
          { id: "loc-de", name: "de" },
        ],
        "CONTENT",
      );

      expect(existsSync("app1/messages")).toBe(true);
      expect(existsSync("app1/messages.de")).toBe(true);
      expect(existsSync("app2/messages")).toBe(true);
      expect(existsSync("app2/messages.de")).toBe(true);
    });
  });

  it("Legacy `pull`/`push` keys are rejected: pull fails naming the missing `targets` key", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", LEGACY_CONFIG);

      await expect(runPull([{ id: "loc-default", name: "default" }], "CONTENT")).rejects.toThrow(
        /targets/,
      );
    });
  });
});
