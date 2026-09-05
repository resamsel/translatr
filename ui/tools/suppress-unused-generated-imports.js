#!/usr/bin/env node
/**
 * openapi-generator's typescript-angular templates unconditionally import
 * symbols (HttpHeaders, HttpParams, error/query-param helper types, ...)
 * that a given operation may not actually use — e.g. a GET with no query
 * params or custom headers never references HttpParams/HttpHeaders. Angular's
 * production build enforces this repo's `noUnusedLocals: true` and fails on
 * those imports with TS6133/TS6192.
 *
 * Rather than relax `noUnusedLocals` project-wide (which would also loosen
 * strictness for hand-written code) or introduce custom Mustache templates,
 * this script mirrors a pattern openapi-generator's own templates already use
 * inconsistently in this exact output (some imports are already preceded by
 * `// @ts-ignore`, e.g. for model types) and extends it to every import
 * declaration where it's actually needed: it inserts a `// @ts-ignore`
 * comment directly above any import statement that has at least one unused
 * named specifier. This suppresses only that one diagnostic, on that one
 * line, in generated files only — hand-written code's strictness is
 * untouched, and nothing here needs maintaining across generator upgrades
 * beyond re-running this script (already wired into `generate:api`).
 */

const fs = require('fs');
const path = require('path');
const ts = require('typescript');

const GENERATED_DIR = path.join(__dirname, '..', 'libs', 'translatr-sdk', 'src', 'lib', 'generated');

function findTsFiles(dir) {
  const out = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      out.push(...findTsFiles(full));
    } else if (entry.isFile() && entry.name.endsWith('.ts')) {
      out.push(full);
    }
  }
  return out;
}

function localImportNames(importClause) {
  const names = [];
  if (!importClause) {
    return names;
  }
  if (importClause.name) {
    names.push(importClause.name.text);
  }
  const bindings = importClause.namedBindings;
  if (bindings && ts.isNamedImports(bindings)) {
    for (const el of bindings.elements) {
      names.push(el.name.text);
    }
  }
  return names;
}

function isUnused(name, textWithoutImport) {
  const re = new RegExp(`\\b${name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\b`);
  return !re.test(textWithoutImport);
}

function processFile(filePath) {
  const original = fs.readFileSync(filePath, 'utf8');
  const source = ts.createSourceFile(filePath, original, ts.ScriptTarget.Latest, true, ts.ScriptKind.TS);

  // Collect insertion points (character offsets) in descending order so
  // earlier offsets stay valid as we splice the string from the end back.
  const insertions = [];

  source.statements.forEach(stmt => {
    if (!ts.isImportDeclaration(stmt) || !stmt.importClause) {
      return;
    }
    const names = localImportNames(stmt.importClause);
    if (names.length === 0) {
      return;
    }

    const before = original.slice(0, stmt.getStart(source));
    const after = original.slice(stmt.getEnd());
    const restOfFile = before + after;

    const anyUnused = names.some(n => isUnused(n, restOfFile));
    if (!anyUnused) {
      return;
    }

    // Skip if already suppressed (a // @ts-ignore on the line immediately above).
    const lineStart = stmt.getStart(source);
    const linesBefore = original.slice(0, lineStart).split('\n');
    const precedingLine = linesBefore[linesBefore.length - 2]; // last full line before this statement's line
    if (precedingLine && precedingLine.trim() === '// @ts-ignore') {
      return;
    }

    insertions.push(lineStart);
  });

  if (insertions.length === 0) {
    return false;
  }

  let updated = original;
  insertions
    .sort((a, b) => b - a)
    .forEach(offset => {
      // Find the start of the line containing `offset`, so the comment lines
      // up at the same indentation and precedes the whole (possibly
      // multi-line) import statement.
      const lineStartOffset = updated.lastIndexOf('\n', offset - 1) + 1;
      const indent = updated.slice(lineStartOffset, offset).match(/^[ \t]*/)[0];
      updated = updated.slice(0, lineStartOffset) + indent + '// @ts-ignore\n' + updated.slice(lineStartOffset);
    });

  fs.writeFileSync(filePath, updated, 'utf8');
  return true;
}

function main() {
  if (!fs.existsSync(GENERATED_DIR)) {
    console.log(`[suppress-unused-generated-imports] ${GENERATED_DIR} does not exist, nothing to do.`);
    return;
  }
  const files = findTsFiles(GENERATED_DIR);
  let changed = 0;
  for (const file of files) {
    if (processFile(file)) {
      changed++;
      console.log(`[suppress-unused-generated-imports] suppressed unused-import diagnostics in ${path.relative(process.cwd(), file)}`);
    }
  }
  console.log(`[suppress-unused-generated-imports] done — ${changed}/${files.length} file(s) modified.`);
}

main();
