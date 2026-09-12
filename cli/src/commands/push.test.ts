import { describe, expect, it } from "bun:test";
import { mkdirSync, writeFileSync } from "node:fs";
import { Command } from "commander";
import { registerPush } from "./push.js";
import { withTempCwd, mockFetch } from "../test-utils.js";

const CONFIG = [
  "translatr:",
  "  endpoint: http://localhost:9000",
  "  access_token: tok",
  "  project_id: proj-1",
  "  default_locale: default",
  "  push:",
  "    file_type: json",
  "    target: conf/messages.?{locale.name}",
].join("\n");

async function runPush(handler: Parameters<typeof mockFetch>[0]) {
  const { fetchFn, calls } = mockFetch(handler);
  const originalFetch = globalThis.fetch;
  globalThis.fetch = fetchFn;
  const logs: string[] = [];
  const errors: string[] = [];
  const originalLog = console.log;
  const originalError = console.error;
  console.log = (...args: unknown[]) => logs.push(args.join(" "));
  console.error = (...args: unknown[]) => errors.push(args.join(" "));
  try {
    const program = new Command();
    program.exitOverride();
    registerPush(program, () => ({}));
    await program.parseAsync(["push"], { from: "user" });
  } finally {
    console.log = originalLog;
    console.error = originalError;
    globalThis.fetch = originalFetch;
  }
  return { calls, logs, errors };
}

describe("`push` uploads matching local files, creating locales as needed", () => {
  it("Pushing a new locale's file creates the locale first", async () => {
    await withTempCwd(async () => {
      mkdirSync("conf", { recursive: true });
      writeFileSync("conf/messages.fr", "FR_CONTENT");
      writeFileSync(".translatr.yml", CONFIG);

      const { calls, logs } = await runPush(({ url, method }) => {
        if (method === "GET" && url.includes("/api/locales/proj-1")) {
          return { json: { list: [] } };
        }
        if (method === "POST" && url.includes("/api/locale?")) {
          return { json: { id: "loc-fr", name: "fr" } };
        }
        if (method === "POST" && url.includes("/import")) {
          return { json: {} };
        }
        throw new Error(`Unexpected request: ${method} ${url}`);
      });

      expect(calls.some((c) => c.method === "POST" && c.url.includes("/import"))).toBe(true);
      expect(logs.join("\n")).toContain("Uploaded conf/messages.fr to fr (new)");
    });
  });

  it("One file's upload failure does not stop the push", async () => {
    await withTempCwd(async () => {
      mkdirSync("conf", { recursive: true });
      writeFileSync("conf/messages.de", "DE_CONTENT");
      writeFileSync("conf/messages.it", "IT_CONTENT");
      writeFileSync(".translatr.yml", CONFIG);

      let importCalls = 0;
      const { logs, errors } = await runPush(({ url, method }) => {
        if (method === "GET" && url.includes("/api/locales/proj-1")) {
          return {
            json: {
              list: [
                { id: "loc-de", name: "de" },
                { id: "loc-it", name: "it" },
              ],
            },
          };
        }
        if (method === "POST" && url.includes("/import")) {
          importCalls += 1;
          if (url.includes("loc-de")) {
            return { status: 400, json: { error: { message: "rejected" } } };
          }
          return { json: {} };
        }
        throw new Error(`Unexpected request: ${method} ${url}`);
      });

      expect(importCalls).toBe(2);
      expect(errors.join("\n")).toContain("rejected");
      expect(logs.join("\n")).toContain("Uploaded conf/messages.it to it");
    });
  });

  it("matches files for a push.target with no literal '?' before {locale.name} (e.g. a path-segment placeholder)", async () => {
    await withTempCwd(async () => {
      const config = [
        "translatr:",
        "  endpoint: http://localhost:9000",
        "  access_token: tok",
        "  project_id: proj-1",
        "  default_locale: default",
        "  push:",
        "    file_type: json",
        "    target: i18n/{locale.name}.json",
      ].join("\n");
      mkdirSync("i18n", { recursive: true });
      writeFileSync("i18n/en.json", "EN_CONTENT");
      writeFileSync(".translatr.yml", config);

      const { calls, logs } = await runPush(({ url, method }) => {
        if (method === "GET" && url.includes("/api/locales/proj-1")) {
          return { json: { list: [{ id: "loc-en", name: "en" }] } };
        }
        if (method === "POST" && url.includes("/import")) {
          return { json: {} };
        }
        throw new Error(`Unexpected request: ${method} ${url}`);
      });

      expect(calls.some((c) => c.method === "POST" && c.url.includes("/import"))).toBe(true);
      expect(logs.join("\n")).toContain("Uploaded i18n/en.json to en");
    });
  });
});
