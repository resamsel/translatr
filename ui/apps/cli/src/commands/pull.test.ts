import { describe, expect, it } from "bun:test";
import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { Command } from "commander";
import { registerPull } from "./pull.js";
import { withTempCwd, mockFetch } from "../test-utils.js";

const CONFIG = [
  "translatr:",
  "  endpoint: http://localhost:9000",
  "  access_token: tok",
  "  project_id: proj-1",
  "  default_locale: default",
  "  pull:",
  "    file_type: json",
  "    target: conf/messages.?{locale.name}",
].join("\n");

async function runPull(locales: { id: string; name: string }[], exportBody: string) {
  const { fetchFn } = mockFetch(({ url, method }) => {
    if (method === "GET" && url.includes("/api/locales/proj-1")) {
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
