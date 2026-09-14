export function printTable(rows: (string | undefined)[][]): void {
  if (rows.length === 0) return;
  const widths: number[] = [];
  for (const row of rows) {
    row.forEach((cell, i) => {
      widths[i] = Math.max(widths[i] ?? 0, (cell ?? "").length);
    });
  }
  for (const row of rows) {
    console.log(row.map((cell, i) => (cell ?? "").padEnd(widths[i])).join("  ").trimEnd());
  }
}

export function eprint(message: string): void {
  console.error(message.trim());
}
