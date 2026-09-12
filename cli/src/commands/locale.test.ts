import { describe, expect, it } from "bun:test";
import { writeFileSync } from "node:fs";
import { Command } from "commander";
import { registerLocale } from "./locale.js";
import { withTempCwd, mockFetch } from "../test-utils.js";

const CONFIG = [
  "translatr:",
  "  endpoint: http://localhost:9000",
  "  access_token: tok",
  "  project_id: proj-1",
].join("\n");

describe("Removing a locale by name", () => {
  it("resolves the name to an ID via search, then deletes it", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG);

      const { fetchFn, calls } = mockFetch(({ url, method }) => {
        if (method === "GET" && url.includes("/api/locales/proj-1")) {
          expect(url).toContain("search=de");
          return { json: { list: [{ id: "loc-de", name: "de" }] } };
        }
        if (method === "DELETE" && url.includes("/api/locale/loc-de")) {
          return { json: { id: "loc-de", name: "de" } };
        }
        throw new Error(`Unexpected request: ${method} ${url}`);
      });
      const originalFetch = globalThis.fetch;
      globalThis.fetch = fetchFn;

      const logs: string[] = [];
      const originalLog = console.log;
      console.log = (...args: unknown[]) => logs.push(args.join(" "));

      try {
        const program = new Command();
        program.exitOverride();
        registerLocale(program, () => ({}));
        await program.parseAsync(["locale", "rm", "de"], { from: "user" });
      } finally {
        console.log = originalLog;
        globalThis.fetch = originalFetch;
      }

      expect(calls.some((c) => c.method === "DELETE")).toBe(true);
      expect(logs.join("\n")).toContain("Locale de has been deleted");
    });
  });

  it("reports a not-found error and exits non-zero when no locale matches the name", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG);

      const { fetchFn } = mockFetch(({ url, method }) => {
        if (method === "GET" && url.includes("/api/locales/proj-1")) {
          return { json: { list: [] } };
        }
        throw new Error(`Unexpected request: ${method} ${url}`);
      });
      const originalFetch = globalThis.fetch;
      globalThis.fetch = fetchFn;

      try {
        const program = new Command();
        program.exitOverride();
        registerLocale(program, () => ({}));
        let thrown: unknown;
        try {
          await program.parseAsync(["locale", "rm", "de"], { from: "user" });
        } catch (e) {
          thrown = e;
        }
        expect(thrown).toBeDefined();
        expect((thrown as Error).message).toContain("Locale with ID 'de' not found");
      } finally {
        globalThis.fetch = originalFetch;
      }
    });
  });
});
