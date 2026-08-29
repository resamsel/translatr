// @ts-check
const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");

module.exports = tseslint.config(
  {
    files: ["**/*.ts"],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      "@angular-eslint/directive-selector": [
        "error",
        {
          type: "attribute",
          prefix: "app",
          style: "camelCase",
        },
      ],
      "@angular-eslint/component-selector": [
        "error",
        {
          type: "element",
          prefix: "app",
          style: "kebab-case",
        },
      ],
      "@typescript-eslint/no-unused-vars": ["warn", { argsIgnorePattern: "^_" }],
      "@typescript-eslint/no-explicit-any": "off",
      "no-console": ["warn", { allow: ["warn", "error"] }],
      "prefer-const": "error",
      // The codebase is NgModule-based throughout, not yet migrated to standalone
      // components. Converting requires Angular's official multi-step schematic plus a
      // full rebuild/regression pass across every app and shared library (a naive
      // eslint --fix attempt here broke the build) -- tracked as separate, deliberate
      // follow-up work rather than blocked on in CI in the meantime.
      "@angular-eslint/prefer-standalone": "warn",
      // Promoted to an error in the angular-eslint v20 recommended set. The codebase
      // uses constructor parameter injection throughout; converting to inject() means
      // running Angular's official schematic across ~265 call sites plus a full
      // regression pass -- deliberate follow-up work, not a CI blocker for the
      // nx/angular platform bump. Same rationale as prefer-standalone above.
      "@angular-eslint/prefer-inject": "warn",
      // Promoted to an error in the angular-eslint v22 recommended set. Angular v22's
      // official `change-detection-eager` migration added `ChangeDetectionStrategy.Eager`
      // to every component in the codebase to preserve pre-v22 runtime change-detection
      // behavior; moving all ~60 components to OnPush is a separate, deliberate refactor
      // plus regression pass, not a CI blocker for the nx/angular platform bump. Same
      // rationale as prefer-standalone / prefer-inject above.
      "@angular-eslint/prefer-on-push-component-change-detection": "warn",
    },
  },
  {
    // Shared component library: selectors here predate any enforced prefix convention
    // (a working mix of "dev-", "app-", and none, referenced by many templates across
    // both apps) -- enforcing "app-" would mean renaming every one of them plus every
    // consuming template, for no functional benefit.
    files: ["libs/translatr-components/**/*.ts"],
    rules: {
      "@angular-eslint/component-selector": "off",
      "@angular-eslint/directive-selector": "off",
    },
  },
  {
    // CLI tools: console output is the actual product here, not debug leftovers.
    files: ["libs/generator/**/*.ts", "apps/lets-generate/**/*.ts"],
    rules: {
      "no-console": "off",
    },
  },
  {
    // translatr-admin's own established convention is mostly "dev-" (one component
    // already uses "app-") -- accept both instead of forcing a rename either way.
    files: ["apps/translatr-admin/**/*.ts"],
    rules: {
      "@angular-eslint/component-selector": [
        "error",
        { type: "element", prefix: ["dev", "app"], style: "kebab-case" },
      ],
    },
  },
  {
    files: ["**/*.html"],
    extends: [
      ...angular.configs.templateRecommended,
      ...angular.configs.templateAccessibility,
    ],
    rules: {},
  }
);

