import { describe, expect, it } from "bun:test";
import { writeFileSync } from "node:fs";
import { Command } from "commander";
import { registerProject } from "./project.js";
import { withTempCwd, mockFetch } from "../test-utils.js";

describe("Project, locale, key, and user management", () => {
  it("Listing projects with a search filter requests the filtered list and prints id/name/owner", async () => {
    await withTempCwd(async () => {
      writeFileSync(
        ".translatr.yml",
        [
          "translatr:",
          "  endpoint: http://localhost:9000",
          "  access_token: tok",
          "  project_id: proj-1",
        ].join("\n"),
      );

      const { fetchFn, calls } = mockFetch(({ url }) => {
        expect(url).toContain("/api/projects");
        expect(url).toContain("search=foo");
        return { json: { list: [{ id: "p1", name: "Foo", ownerName: "alice" }] } };
      });
      const originalFetch = globalThis.fetch;
      globalThis.fetch = fetchFn;

      const logs: string[] = [];
      const originalLog = console.log;
      console.log = (...args: unknown[]) => logs.push(args.join(" "));

      try {
        const program = new Command();
        program.exitOverride();
        registerProject(program, () => ({}));
        await program.parseAsync(["project", "ls", "foo"], { from: "user" });
      } finally {
        console.log = originalLog;
        globalThis.fetch = originalFetch;
      }

      expect(calls.length).toBe(1);
      const output = logs.join("\n");
      expect(output).toContain("p1");
      expect(output).toContain("Foo");
      expect(output).toContain("alice");
    });
  });
});
