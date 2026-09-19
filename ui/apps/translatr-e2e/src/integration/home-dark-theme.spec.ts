import type { Page } from '@playwright/test';
import { test, expect } from '../support/test';
import { mockUnauthenticated } from '../support/mock-api';

type Rgb = [number, number, number];

interface SectionReport {
  selector: string;
  text: string;
  color: Rgb;
  background: Rgb;
  fontSize: number;
  fontWeight: number;
}

/**
 * Resolves each element's text color and its *effective* background (the first
 * non-transparent background walking up the tree, composited over white) in the
 * browser, so the assertions below work on what is actually painted.
 */
async function measure(page: Page, selector: string): Promise<SectionReport[]> {
  return page.evaluate((sel) => {
    const parse = (value: string): [number, number, number, number] => {
      const m = value.match(/rgba?\(([^)]+)\)/);
      if (!m) {
        return [0, 0, 0, 0];
      }
      const [r, g, b, a] = m[1].split(',').map((p) => parseFloat(p.trim()));
      return [r, g, b, a === undefined ? 1 : a];
    };
    const effectiveBackground = (el: Element): [number, number, number] => {
      const layers: [number, number, number, number][] = [];
      for (let node: Element | null = el; node; node = node.parentElement) {
        const bg = parse(getComputedStyle(node).backgroundColor);
        if (bg[3] > 0) {
          layers.push(bg);
          if (bg[3] === 1) {
            break;
          }
        }
      }
      return layers.reduceRight<[number, number, number]>(
        (under, [r, g, b, a]) => [
          r * a + under[0] * (1 - a),
          g * a + under[1] * (1 - a),
          b * a + under[2] * (1 - a),
        ],
        [255, 255, 255],
      );
    };
    return Array.from(document.querySelectorAll(sel)).map((el) => {
      const style = getComputedStyle(el);
      const [r, g, b] = parse(style.color);
      return {
        selector: sel,
        text: (el.textContent ?? '').trim().slice(0, 40),
        color: [r, g, b] as [number, number, number],
        background: effectiveBackground(el),
        fontSize: parseFloat(style.fontSize),
        fontWeight: parseInt(style.fontWeight, 10),
      };
    });
  }, selector);
}

function luminance([r, g, b]: Rgb): number {
  const [lr, lg, lb] = [r, g, b].map((c) => {
    const s = c / 255;
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * lr + 0.7152 * lg + 0.0722 * lb;
}

function contrast(a: Rgb, b: Rgb): number {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
}

/** WCAG "large text": >= 24px, or >= 18.66px bold. */
function isLarge({ fontSize, fontWeight }: SectionReport): boolean {
  return fontSize >= 24 || (fontSize >= 18.66 && fontWeight >= 700);
}

async function openHome(page: Page, theme: 'light' | 'dark'): Promise<void> {
  await mockUnauthenticated(page);
  await page.goto('');
  await expect(page.locator('h1')).toBeVisible();
  // The app applies the theme as a body class (ThemeService); set it directly so the
  // test does not depend on the ThemeSwitcher flag or the OS preference.
  await page.evaluate((t) => {
    document.body.classList.remove('light-theme', 'dark-theme');
    document.body.classList.add(`${t}-theme`);
  }, theme);
}

const BODY_TEXT = '.container p, .alternate-background p';
const HEADINGS = '.container h3.header, .alternate-background h3.header, .container h4, .alternate-background h4';

test.describe('Home page dark theme (#351)', () => {
  test('Dark theme, all sections themed: no section keeps a white background', async ({ page }) => {
    await openHome(page, 'dark');

    // Contract: the alternating bands carry the `alternate-background` class (was `white`).
    const bands = page.locator('.alternate-background');
    await expect(bands).not.toHaveCount(0);
    await expect(page.locator('.white')).toHaveCount(0);

    const reports = await measure(page, '.alternate-background');
    for (const band of reports) {
      expect(luminance(band.background), `band background ${band.background}`).toBeLessThan(0.2);
    }

    // Plain sections have no background of their own: what shows through must be dark too.
    for (const section of await measure(page, '.container > section.section')) {
      expect(luminance(section.background), `section background ${section.background}`).toBeLessThan(0.2);
    }
  });

  test('Dark theme, text legible: headings and body text meet WCAG AA', async ({ page }) => {
    await openHome(page, 'dark');

    const paragraphs = await measure(page, BODY_TEXT);
    expect(paragraphs.length).toBeGreaterThan(0);
    for (const p of paragraphs) {
      expect(contrast(p.color, p.background), `paragraph "${p.text}"`).toBeGreaterThanOrEqual(4.5);
    }

    const headings = await measure(page, HEADINGS);
    expect(headings.length).toBeGreaterThan(0);
    for (const h of headings) {
      const required = isLarge(h) ? 3 : 4.5;
      expect(contrast(h.color, h.background), `heading "${h.text}"`).toBeGreaterThanOrEqual(required);
    }
  });

  test('Activity section in dark theme: text and graph sit on the dark surface', async ({ page }) => {
    await openHome(page, 'dark');

    const activity = page.locator('.alternate-background', { has: page.locator('dev-activity-graph') });
    await expect(activity).toHaveCount(1);
    await expect(activity.locator('dev-activity-graph')).toBeVisible();

    const [bg] = await measure(page, '.alternate-background:has(dev-activity-graph)');
    expect(luminance(bg.background)).toBeLessThan(0.2);

    const [heading] = await measure(page, '.alternate-background:has(dev-activity-graph) h3.header');
    expect(contrast(heading.color, heading.background)).toBeGreaterThanOrEqual(3);
    const [copy] = await measure(page, '.alternate-background:has(dev-activity-graph) p');
    expect(contrast(copy.color, copy.background)).toBeGreaterThanOrEqual(4.5);
  });

  test('Light theme unchanged: bands stay white, text and headings keep their colors', async ({ page }) => {
    await openHome(page, 'light');

    await expect(page.locator('.alternate-background')).not.toHaveCount(0);
    for (const band of await measure(page, '.alternate-background')) {
      expect(band.background).toEqual([255, 255, 255]);
    }

    const [copy] = await measure(page, '.container .promo p');
    expect(copy.color.map(Math.round)).toEqual([0, 0, 0]);
    const [heading] = await measure(page, '.container h3.header');
    expect(heading.color).toEqual([67, 160, 71]);
  });
});
