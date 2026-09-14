import { describe, expect, it } from "bun:test";
import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";

// Requirement: CLI is an Nx-integrated workspace project
// (openspec/changes/adopt-translatr-sdk-and-relocate-cli/specs/translatr-cli/spec.md)

const CLI_ROOT = join(import.meta.dir, "..");

describe("CLI is an Nx-integrated workspace project", () => {
  it("lives at ui/apps/cli, registered as an Nx project", () => {
    const projectJsonPath = join(CLI_ROOT, "project.json");
    expect(existsSync(projectJsonPath)).toBe(true);

    const projectJson = JSON.parse(readFileSync(projectJsonPath, "utf8")) as {
      name?: string;
      projectType?: string;
      targets?: Record<string, unknown>;
    };
    expect(projectJson.name).toBe("cli");
    expect(projectJson.projectType).toBe("application");
    expect(Object.keys(projectJson.targets ?? {})).toEqual(
      expect.arrayContaining(["build", "dev", "test", "typecheck"]),
    );
  });

  it("keeps its own Bun toolchain independent of ui/'s npm-managed dependencies", () => {
    // The CLI must still own its package.json/lockfile at the new location -
    // it does not get folded into ui/package.json's dependency tree.
    expect(existsSync(join(CLI_ROOT, "package.json"))).toBe(true);
    expect(existsSync(join(CLI_ROOT, "bun.lock"))).toBe(true);

    const cliPackageJson = JSON.parse(
      readFileSync(join(CLI_ROOT, "package.json"), "utf8"),
    ) as { name?: string; scripts?: Record<string, string> };
    expect(cliPackageJson.name).toBe("@translatr/cli");
    // The Bun build/dev scripts that Nx's project.json targets delegate to
    // must still exist and still be Bun invocations.
    expect(cliPackageJson.scripts?.build).toContain("bun build");
    expect(cliPackageJson.scripts?.dev).toContain("bun run src/index.ts");
  });

  it("the old repo-root cli/ location is gone (fully relocated, not copied)", () => {
    const oldRoot = join(CLI_ROOT, "..", "..", "..", "cli");
    expect(existsSync(join(oldRoot, "src", "index.ts"))).toBe(false);
  });
});
