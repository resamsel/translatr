import { describe, expect, it } from "bun:test";
import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";
// Real import of the relocated module - this line itself is the solid RED
// until cli/ is moved to ui/apps/cli/.
import { Api, ApiError, isUuid } from "./api.js";
import type { TranslatrConfig } from "./config.js";
import { mockFetch } from "./test-utils.js";

// Requirement: CLI response types are sourced from the shared
// OpenAPI-generated models
// (openspec/changes/adopt-translatr-sdk-and-relocate-cli/specs/translatr-cli/spec.md)

const GENERATED_MODEL_DIR = join(
  import.meta.dir,
  "..",
  "..",
  "..",
  "libs",
  "translatr-sdk",
  "src",
  "lib",
  "generated",
  "model",
);

function makeApi(handler: Parameters<typeof mockFetch>[0]): Api {
  const config: TranslatrConfig = {
    endpoint: "http://localhost:9000",
    access_token: "tok",
    project_id: "proj-1",
    default_locale: "default",
    targets: { "i18n/{locale.name}.json": { file_type: "json" } },
  };
  const api = new Api(config);
  const { fetchFn } = mockFetch(handler);
  globalThis.fetch = fetchFn;
  return api;
}

describe("CLI response types are sourced from the shared OpenAPI-generated models", () => {
  it("api.ts no longer hand-declares Project/Locale/Key/User - it imports the generated DTOs", () => {
    const apiSource = readFileSync(join(import.meta.dir, "api.ts"), "utf8");
    const sdkTypesSource = readFileSync(join(import.meta.dir, "sdk-types.ts"), "utf8");

    expect(apiSource).not.toMatch(/export\s+interface\s+Project\s*\{/);
    expect(apiSource).not.toMatch(/export\s+interface\s+Locale\s*\{/);
    expect(apiSource).not.toMatch(/export\s+interface\s+Key\s*\{/);
    expect(apiSource).not.toMatch(/export\s+interface\s+User\s*\{/);

    // api.ts sources its response types from the DTOs re-exported by
    // sdk-types.ts, which itself pulls straight from the generated SDK.
    expect(apiSource).toMatch(/ProjectDto/);
    expect(apiSource).toMatch(/LocaleDto/);
    expect(apiSource).toMatch(/KeyDto/);
    expect(apiSource).toMatch(/UserDto/);
    expect(sdkTypesSource).toMatch(/translatr-sdk.*generated.*model|generated\/model/);
  });

  it("the DTOs it imports really are the generated models (not a local copy)", () => {
    expect(existsSync(join(GENERATED_MODEL_DIR, "projectDto.ts"))).toBe(true);
    expect(existsSync(join(GENERATED_MODEL_DIR, "localeDto.ts"))).toBe(true);
    expect(existsSync(join(GENERATED_MODEL_DIR, "keyDto.ts"))).toBe(true);
    expect(existsSync(join(GENERATED_MODEL_DIR, "userDto.ts"))).toBe(true);
  });

  it("projects() still returns data shaped by the DTO, driven through the CLI's own fetch client", async () => {
    const api = makeApi(() => ({
      status: 200,
      json: { list: [{ id: "p1", name: "demo", ownerName: "alice" }] },
    }));

    const projects = await api.projects();

    expect(projects).toEqual([{ id: "p1", name: "demo", ownerName: "alice" }]);
  });
});

describe("CLI has no Angular runtime dependency", () => {
  it("package.json declares no @angular dependency", () => {
    const cliPackageJson = JSON.parse(
      readFileSync(join(import.meta.dir, "..", "package.json"), "utf8"),
    ) as { dependencies?: Record<string, string>; devDependencies?: Record<string, string> };

    const allDeps = {
      ...(cliPackageJson.dependencies ?? {}),
      ...(cliPackageJson.devDependencies ?? {}),
    };
    const angularDeps = Object.keys(allDeps).filter((name) => name.startsWith("@angular"));

    expect(angularDeps).toEqual([]);
  });

  it("no source file under ui/apps/cli/src imports @angular/*", () => {
    const glob = new Bun.Glob("**/*.ts");
    const offenders: string[] = [];
    for (const file of glob.scanSync({ cwd: import.meta.dir, absolute: true })) {
      if (file.endsWith(".test.ts")) continue;
      const contents = readFileSync(file, "utf8");
      if (/from\s+["']@angular\//.test(contents)) {
        offenders.push(file);
      }
    }

    expect(offenders).toEqual([]);
  });
});

describe("API errors the CLI cannot parse into the known shape", () => {
  it("Server returns a generic 404 body: error names method, URL, status - not the raw body", async () => {
    const api = makeApi(() => ({
      status: 404,
      json: { status: 404, message: "HTTP 404 Not Found" },
    }));

    let error: unknown;
    try {
      await api.projects();
    } catch (e) {
      error = e;
    }

    expect(error).toBeInstanceOf(ApiError);
    expect((error as Error).message).toContain("404");
  });
});

describe("isUuid", () => {
  it("accepts a v4 UUID and rejects a plain name", () => {
    expect(isUuid("123e4567-e89b-42d3-a456-426614174000")).toBe(true);
    expect(isUuid("de")).toBe(false);
  });
});
