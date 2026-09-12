# translatr-cli

Cross-platform (Windows, Linux, macOS) command-line client for a Translatr
server: manage projects, locales and keys, and push/pull translation files.
It's a TypeScript rewrite of the old [`src/python/translatr.py`](../src/python/translatr.py)
script, with the same `.translatr.yml` config format and commands.

## Install

**Via npm** (needs Node.js 18+):

```bash
npm install -g @translatr/cli
```

**Standalone binary** (no runtime needed) — download the binary for your
platform from the release assets and put it on your `PATH` as `translatr`:

- `translatr-linux-x64`, `translatr-linux-arm64`
- `translatr-macos-x64`, `translatr-macos-arm64`
- `translatr-windows-x64.exe`

## Usage

```bash
translatr init <endpoint> <access_token> <project_id>
translatr config
translatr project ls|create|rm
translatr locale ls|create|rm
translatr key ls|create|rm
translatr user ls
translatr push
translatr pull
```

Run `translatr --help` or `translatr <command> --help` for details.

## Development

Requires [Bun](https://bun.sh).

```bash
bun install
bun run dev -- --help   # run from source
bun run typecheck
bun run build           # bundles dist/index.js for npm (runs under Node)
bun run compile:all     # produces standalone native binaries in dist/bin
```
