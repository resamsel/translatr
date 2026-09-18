import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let mediaQueryListeners: Array<(e: Partial<MediaQueryListEvent>) => void>;

  const setupMatchMedia = (matches: boolean) => {
    mediaQueryListeners = [];
    window.matchMedia = jest.fn().mockImplementation((query: string) => ({
      matches,
      media: query,
      addEventListener: (_type: string, cb: (e: Partial<MediaQueryListEvent>) => void) =>
        mediaQueryListeners.push(cb),
      removeEventListener: jest.fn()
    })) as never;
  };

  beforeEach(() => {
    localStorage.clear();
    document.body.className = '';
    setupMatchMedia(false);
    TestBed.configureTestingModule({});
  });

  it('defaults preference to "system" when nothing is stored', () => {
    const service = TestBed.inject(ThemeService);

    let preference: string | undefined;
    service.preference$.subscribe(p => (preference = p));

    expect(preference).toBe('system');
  });

  it('resolves the effective theme to "light" when preference is set to "light"', () => {
    const service = TestBed.inject(ThemeService);
    service.setPreference('light');

    let effective: string | undefined;
    service.effectiveTheme$.subscribe(t => (effective = t));

    expect(effective).toBe('light');
  });

  it('resolves the effective theme to "dark" when preference is set to "dark"', () => {
    const service = TestBed.inject(ThemeService);
    service.setPreference('dark');

    let effective: string | undefined;
    service.effectiveTheme$.subscribe(t => (effective = t));

    expect(effective).toBe('dark');
  });

  it('resolves "system" preference from the current OS color scheme', () => {
    setupMatchMedia(true);
    const service = TestBed.inject(ThemeService);
    service.setPreference('system');

    let effective: string | undefined;
    service.effectiveTheme$.subscribe(t => (effective = t));

    expect(effective).toBe('dark');
  });

  it('updates the effective theme live when the OS color scheme changes while preference is "system"', () => {
    setupMatchMedia(false);
    const service = TestBed.inject(ThemeService);
    service.setPreference('system');

    const results: string[] = [];
    service.effectiveTheme$.subscribe(t => results.push(t));
    expect(results).toEqual(['light']);

    mediaQueryListeners.forEach(cb => cb({ matches: true }));

    expect(results[results.length - 1]).toBe('dark');
  });

  it('persists the chosen preference to localStorage', () => {
    const service = TestBed.inject(ThemeService);
    service.setPreference('dark');

    expect(localStorage.getItem('translatr-theme')).toBe('dark');
  });

  it('reads a previously stored preference back on a fresh instance (reload)', () => {
    const first = TestBed.inject(ThemeService);
    first.setPreference('dark');

    TestBed.resetTestingModule();
    setupMatchMedia(false);
    TestBed.configureTestingModule({});
    const reloaded = TestBed.inject(ThemeService);

    let preference: string | undefined;
    reloaded.preference$.subscribe(p => (preference = p));

    expect(preference).toBe('dark');
  });

  it('applies the effective theme as a class on document.body', () => {
    const service = TestBed.inject(ThemeService);
    service.setPreference('dark');

    expect(document.body.classList.contains('dark-theme')).toBe(true);
    expect(document.body.classList.contains('light-theme')).toBe(false);

    service.setPreference('light');

    expect(document.body.classList.contains('light-theme')).toBe(true);
    expect(document.body.classList.contains('dark-theme')).toBe(false);
  });
});
