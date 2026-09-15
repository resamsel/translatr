import { describe, expect, it } from "bun:test";
import { writeFileSync } from "node:fs";
import { Command } from "commander";
import { registerKey } from "./key.js";
import { withTempCwd, mockFetch } from "../test-utils.js";

const CONFIG = [
  "endpoint: http://localhost:9000",
  "access_token: tok",
  "project_id: proj-1",
].join("\n");

describe("Listing keys for the configured project", () => {
  it("requests keys from the project-scoped contract path", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG);

      const { fetchFn, calls } = mockFetch(({ url, method }) => {
        if (method === "GET" && url.includes("/api/project/proj-1/keys")) {
          expect(url).toContain("search=foo");
          return { json: { list: [{ id: "key-1", name: "foo" }] } };
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
        registerKey(program, () => ({}));
        await program.parseAsync(["key", "ls", "foo"], { from: "user" });
      } finally {
        console.log = originalLog;
        globalThis.fetch = originalFetch;
      }

      expect(calls.some((c) => c.method === "GET" && c.url.includes("/api/project/proj-1/keys"))).toBe(
        true,
      );
      expect(logs.join("\n")).toContain("foo");
    });
  });
});

describe("Removing a key by name", () => {
  it("resolves the name to an ID via the project-scoped search, then deletes it", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG);

      const { fetchFn, calls } = mockFetch(({ url, method }) => {
        if (method === "GET" && url.includes("/api/project/proj-1/keys")) {
          expect(url).toContain("search=foo");
          return { json: { list: [{ id: "key-1", name: "foo" }] } };
        }
        if (method === "DELETE" && url.includes("/api/key/key-1")) {
          return { json: { id: "key-1", name: "foo" } };
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
        registerKey(program, () => ({}));
        await program.parseAsync(["key", "rm", "foo"], { from: "user" });
      } finally {
        console.log = originalLog;
        globalThis.fetch = originalFetch;
      }

      expect(calls.some((c) => c.method === "DELETE")).toBe(true);
      expect(logs.join("\n")).toContain("Key foo has been deleted");
    });
  });

  it("reports a not-found error and exits non-zero when no key matches the name", async () => {
    await withTempCwd(async () => {
      writeFileSync(".translatr.yml", CONFIG);

      const { fetchFn } = mockFetch(({ url, method }) => {
        if (method === "GET" && url.includes("/api/project/proj-1/keys")) {
          return { json: { list: [] } };
        }
        throw new Error(`Unexpected request: ${method} ${url}`);
      });
      const originalFetch = globalThis.fetch;
      globalThis.fetch = fetchFn;

      try {
        const program = new Command();
        program.exitOverride();
        registerKey(program, () => ({}));
        let thrown: unknown;
        try {
          await program.parseAsync(["key", "rm", "foo"], { from: "user" });
        } catch (e) {
          thrown = e;
        }
        expect(thrown).toBeDefined();
        expect((thrown as Error).message).toContain("Key with ID 'foo' not found");
      } finally {
        globalThis.fetch = originalFetch;
      }
    });
  });
});
