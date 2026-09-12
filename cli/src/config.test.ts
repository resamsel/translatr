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
