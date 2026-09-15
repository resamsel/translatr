# translatr-cli

Cross-platform (Windows, Linux, macOS) command-line client for a Translatr
server: manage projects, locales and keys, and push/pull translation files.

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

`translatr init` writes a `.translatr.yml` config file with the given values;
every setting is a top-level key, and `targets` maps each translation file's
location to its file type:

```yaml
endpoint: https://translatr.example
access_token: ${TRANSLATR_ACCESS_TOKEN}
project_id: my-project-id
default_locale: default
targets:
  conf/messages.?{locale.name}:
    file_type: play_messages
```

## Development

Requires [Bun](https://bun.sh).

```bash
bun install
bun run dev -- --help   # run from source
bun run typecheck
bun run build           # bundles dist/index.js for npm (runs under Node)
bun run compile:all     # produces standalone native binaries in dist/bin
```
