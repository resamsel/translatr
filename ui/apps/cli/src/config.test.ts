import { describe, expect, it } from "bun:test";
import { readFileSync, writeFileSync } from "node:fs";
import { readConfig, readConfigMerge, ConfigError } from "./config.js";
import { withTempCwd } from "./test-utils.js";

describe("Config file drives every command", () => {
  it("Missing config file: readConfig throws and tells the user to run `translatr init`", async () => {
    await withTempCwd(() => {
      expect(() => readConfig()).toThrow(ConfigError);
      try {
        readConfig();
        throw new Error("expected readConfig to throw");
      } catch (e) {
        expect(e).toBeInstanceOf(ConfigError);
        expect((e as Error).message).toContain("translatr init");
      }
    });
  });

  it("Required env var missing: readConfig raises an error naming the missing variable", async () => {
    await withTempCwd(() => {
      writeFileSync(
        ".translatr.yml",
        [
          "translatr:",
          "  endpoint: http://localhost:9000",
          "  access_token: ${TRANSLATR_CLI_TEST_MISSING_VAR}",
          "  project_id: proj-1",
        ].join("\n"),
      );
      delete process.env.TRANSLATR_CLI_TEST_MISSING_VAR;

      expect(() => readConfig()).toThrow(/TRANSLATR_CLI_TEST_MISSING_VAR/);
    });
  });

  it("CLI flag overrides config value without writing it back to the file", async () => {
    await withTempCwd(() => {
      const original = [
        "translatr:",
        "  endpoint: http://config-endpoint.example",
        "  access_token: tok",
        "  project_id: proj-1",
      ].join("\n");
      writeFileSync(".translatr.yml", original);

      const config = readConfigMerge({ endpoint: "https://other.example" });

      expect(config.endpoint).toBe("https://other.example");
      expect(readFileSync(".translatr.yml", "utf8")).toBe(original);
    });
  });
});

describe("`.env` loading is opt-in via `load_dotenv`", () => {
  it("Switch absent: .env is not read, ${VAR} substitution only sees the process environment", async () => {
    await withTempCwd(() => {
      delete process.env.TRANSLATR_CLI_TEST_DOTENV_ABSENT;
      writeFileSync(".env", "TRANSLATR_CLI_TEST_DOTENV_ABSENT=from-dotenv\n");
      writeFileSync(
        ".translatr.yml",
        [
          "translatr:",
          "  endpoint: http://localhost:9000",
          "  access_token: ${TRANSLATR_CLI_TEST_DOTENV_ABSENT}",
          "  project_id: proj-1",
        ].join("\n"),
      );

      expect(() => readConfig()).toThrow(/TRANSLATR_CLI_TEST_DOTENV_ABSENT/);
    });
  });

  it("Switch explicitly disabled: .env is not read even though it exists", async () => {
    await withTempCwd(() => {
      delete process.env.TRANSLATR_CLI_TEST_DOTENV_DISABLED;
      writeFileSync(".env", "TRANSLATR_CLI_TEST_DOTENV_DISABLED=from-dotenv\n");
      writeFileSync(
        ".translatr.yml",
        [
          "translatr:",
          "  endpoint: http://localhost:9000",
          "  access_token: ${TRANSLATR_CLI_TEST_DOTENV_DISABLED}",
          "  project_id: proj-1",
          "  load_dotenv: false",
        ].join("\n"),
      );

      expect(() => readConfig()).toThrow(/TRANSLATR_CLI_TEST_DOTENV_DISABLED/);
    });
  });
});

describe("Enabled switch loads `.env` before substitution", () => {
  it("`.env` present and enabled: its values resolve ${VAR} substitutions", async () => {
    await withTempCwd(() => {
      delete process.env.TRANSLATR_CLI_TEST_DOTENV_PRESENT;
      try {
        writeFileSync(".env", "TRANSLATR_CLI_TEST_DOTENV_PRESENT=abc123\n");
        writeFileSync(
          ".translatr.yml",
          [
            "translatr:",
            "  endpoint: http://localhost:9000",
            "  access_token: ${TRANSLATR_CLI_TEST_DOTENV_PRESENT}",
            "  project_id: proj-1",
            "  load_dotenv: true",
          ].join("\n"),
        );

        const config = readConfig();

        expect(config.access_token).toBe("abc123");
      } finally {
        delete process.env.TRANSLATR_CLI_TEST_DOTENV_PRESENT;
      }
    });
  });

  it("`.env` missing and enabled: proceeds without error, behaving as if disabled", async () => {
    await withTempCwd(() => {
      process.env.TRANSLATR_CLI_TEST_DOTENV_MISSING_FILE = "already-in-shell";
      try {
        writeFileSync(
          ".translatr.yml",
          [
            "translatr:",
            "  endpoint: http://localhost:9000",
            "  access_token: ${TRANSLATR_CLI_TEST_DOTENV_MISSING_FILE}",
            "  project_id: proj-1",
            "  load_dotenv: true",
          ].join("\n"),
        );

        const config = readConfig();

        expect(config.access_token).toBe("already-in-shell");
      } finally {
        delete process.env.TRANSLATR_CLI_TEST_DOTENV_MISSING_FILE;
      }
    });
  });
});

describe("Existing process environment takes precedence", () => {
  it("Shell variable overrides `.env`: the process environment's value wins", async () => {
    await withTempCwd(() => {
      process.env.TRANSLATR_CLI_TEST_DOTENV_PRECEDENCE = "from-shell";
      try {
        writeFileSync(".env", "TRANSLATR_CLI_TEST_DOTENV_PRECEDENCE=from-dotenv\n");
        writeFileSync(
          ".translatr.yml",
          [
            "translatr:",
            "  endpoint: http://localhost:9000",
            "  access_token: ${TRANSLATR_CLI_TEST_DOTENV_PRECEDENCE}",
            "  project_id: proj-1",
            "  load_dotenv: true",
          ].join("\n"),
        );

        const config = readConfig();

        expect(config.access_token).toBe("from-shell");
      } finally {
        delete process.env.TRANSLATR_CLI_TEST_DOTENV_PRECEDENCE;
      }
    });
  });
});
