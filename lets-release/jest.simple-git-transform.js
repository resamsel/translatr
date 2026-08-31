/**
 * Jest 26's module runtime does not resolve the `node:` protocol prefix for
 * Node core modules. simple-git >= 3.24 emits `require("node:events")` /
 * `require("node:path")` in its CommonJS bundle, which makes the test runner
 * fail with `ENOENT: ... open 'events'`.
 *
 * This transform is scoped to simple-git only (see `transform` /
 * `transformIgnorePatterns` in package.json) and simply drops the prefix so
 * the builtins load normally. It can be removed once the test runner is on
 * Jest >= 27, which understands `node:` natively.
 */
module.exports = {
  process(src) {
    return { code: src.replace(/require\("node:([^"]+)"\)/g, 'require("$1")') };
  },
  getCacheKey() {
    return 'simple-git-node-protocol-shim-v1';
  }
};
