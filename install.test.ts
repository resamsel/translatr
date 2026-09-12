import { describe, expect, it } from "bun:test";
import { chmodSync, existsSync, mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { spawnSync } from "node:child_process";

const INSTALL_SH = join(import.meta.dir, "install.sh");

function runInstall(opts: {
  unameS: string;
  unameM: string;
  prefix: string;
  stubDir: string;
  curlLog: string;
  tag?: string;
}) {
  const stubUname = `#!/bin/bash
case "$1" in
  -s) echo "${opts.unameS}" ;;
  -m) echo "${opts.unameM}" ;;
esac
`;
  // The "binary" curl fakes downloading must itself be a runnable script,
  // since install.sh chmod's it and then executes it with -h to smoke-test
  // the install (mirroring what a real translatr binary would do).
  const stubCurl = `#!/bin/bash
echo "$@" >> "${opts.curlLog}"
out=""
prev=""
for arg in "$@"; do
  if [ "$prev" = "-o" ]; then
    out="$arg"
  fi
  prev="$arg"
done
FAKE_BINARY_CONTENT='#!/bin/bash
echo "translatr FAKE_VERSION"
'
if [ -n "$out" ]; then
  printf '%s' "$FAKE_BINARY_CONTENT" > "$out"
else
  printf '%s' "$FAKE_BINARY_CONTENT"
fi
exit 0
`;
  writeFileSync(join(opts.stubDir, "uname"), stubUname);
  writeFileSync(join(opts.stubDir, "curl"), stubCurl);
  chmodSync(join(opts.stubDir, "uname"), 0o755);
  chmodSync(join(opts.stubDir, "curl"), 0o755);

  const env: Record<string, string> = {
    ...process.env,
    PATH: `${opts.stubDir}:${process.env.PATH}`,
    TRANSLATR_PREFIX: opts.prefix,
  } as Record<string, string>;
  if (opts.tag) env.TRANSLATR_TAG = opts.tag;

  return spawnSync("bash", [INSTALL_SH], { env, encoding: "utf8" });
}

describe("One-line installer fetches a working binary", () => {
  it("Installing on a supported platform places an executable translatr on the PATH", () => {
    const stubDir = mkdtempSync(join(tmpdir(), "translatr-install-stub-"));
    const prefix = mkdtempSync(join(tmpdir(), "translatr-install-prefix-"));
    const curlLog = join(stubDir, "curl.log");
    writeFileSync(curlLog, "");
    mkdirSync(join(prefix, "bin"), { recursive: true });

    try {
      const result = runInstall({
        unameS: "Linux",
        unameM: "x86_64",
        prefix,
        stubDir,
        curlLog,
      });

      expect(result.status).toBe(0);

      const installedBinary = join(prefix, "bin", "translatr");
      expect(existsSync(installedBinary)).toBe(true);

      const curlInvocation = readFileSync(curlLog, "utf8");
      expect(curlInvocation).toContain("translatr-linux-x64");
    } finally {
      rmSync(stubDir, { recursive: true, force: true });
      rmSync(prefix, { recursive: true, force: true });
    }
  });

  it("Unsupported platform fails with a clear error naming the detected platform", () => {
    const stubDir = mkdtempSync(join(tmpdir(), "translatr-install-stub-"));
    const prefix = mkdtempSync(join(tmpdir(), "translatr-install-prefix-"));
    const curlLog = join(stubDir, "curl.log");
    writeFileSync(curlLog, "");
    mkdirSync(join(prefix, "bin"), { recursive: true });

    try {
      const result = runInstall({
        unameS: "SunOS",
        unameM: "sparc64",
        prefix,
        stubDir,
        curlLog,
      });

      expect(result.status).not.toBe(0);
      expect(`${result.stdout}${result.stderr}`).toContain("sparc64");
      expect(existsSync(join(prefix, "bin", "translatr"))).toBe(false);
    } finally {
      rmSync(stubDir, { recursive: true, force: true });
      rmSync(prefix, { recursive: true, force: true });
    }
  });
});
