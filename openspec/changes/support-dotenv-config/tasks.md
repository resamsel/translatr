## 1. Dependency and parsing

- [x] 1.1 Add `dotenv` to `cli/package.json` dependencies and install - verify `node_modules/dotenv` present and `npm run build` (or the CLI's build script) succeeds

## 2. Config loading

- [x] 2.1 In `readConfig()` (`cli/src/config.ts`), read `load_dotenv` off the raw parsed YAML (`raw.translatr?.load_dotenv`) before calling `substitute()` - verify via unit test that a `false`/absent value does not attempt to read any `.env` file
- [x] 2.2 When `load_dotenv` is `true`, read `.env` from the current working directory if it exists, parse it with `dotenv.parse`, and merge entries into `process.env` without overwriting keys already set - verify via unit test with a temp `.env` file and a pre-set `process.env` key of the same name (shell value wins)
- [x] 2.3 Confirm a missing `.env` file with `load_dotenv: true` does not throw - verify via unit test asserting `readConfig()` completes normally with no `.env` present
- [x] 2.4 Update the `TranslatrConfig` interface and any relevant JSDoc/comments to include the optional `load_dotenv` key - verify by `tsc`/typecheck passing with a test config object that sets it

## 3. Tests

- [x] 3.1 Add/extend `cli/src/config.test.ts` covering: switch absent (no `.env` read even if file exists), switch `false`, switch `true` with `.env` present, switch `true` with `.env` missing, and shell-vs-`.env` precedence - verify with `npm test` passing
