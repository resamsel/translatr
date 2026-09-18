import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, combineLatest, Observable } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';

export type ThemePreference = 'light' | 'dark' | 'system';
export type EffectiveTheme = 'light' | 'dark';

const STORAGE_KEY = 'translatr-theme';
const DARK_MEDIA_QUERY = '(prefers-color-scheme: dark)';

@Injectable({ providedIn: 'root' })
export class ThemeService implements OnDestroy {
  private readonly mediaQuery: MediaQueryList | undefined =
    typeof window !== 'undefined' && typeof window.matchMedia === 'function'
      ? window.matchMedia(DARK_MEDIA_QUERY)
      : undefined;

  private readonly mediaQueryListener = (event: MediaQueryListEvent) =>
    this.systemPrefersDark$.next(event.matches);

  private readonly preferenceSubject = new BehaviorSubject<ThemePreference>(
    this.readStoredPreference()
  );
  private readonly systemPrefersDark$ = new BehaviorSubject<boolean>(this.mediaQuery?.matches ?? false);

  readonly preference$: Observable<ThemePreference> = this.preferenceSubject.asObservable();

  readonly effectiveTheme$: Observable<EffectiveTheme> = combineLatest([
    this.preferenceSubject,
    this.systemPrefersDark$
  ]).pipe(
    map(([preference, systemPrefersDark]) => this.resolveEffective(preference, systemPrefersDark)),
    distinctUntilChanged()
  );

  private readonly bodySubscription = this.effectiveTheme$.subscribe(theme => this.applyToBody(theme));

  constructor() {
    this.mediaQuery?.addEventListener?.('change', this.mediaQueryListener);
  }

  ngOnDestroy(): void {
    this.mediaQuery?.removeEventListener?.('change', this.mediaQueryListener);
    this.bodySubscription.unsubscribe();
  }

  setPreference(preference: ThemePreference): void {
    this.preferenceSubject.next(preference);
    try {
      localStorage.setItem(STORAGE_KEY, preference);
    } catch {
      // localStorage unavailable (e.g. private browsing) - preference stays in-memory only
    }
  }

  private readStoredPreference(): ThemePreference {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === 'light' || stored === 'dark' || stored === 'system') {
        return stored;
      }
    } catch {
      // localStorage unavailable - fall through to the default
    }
    return 'system';
  }

  private resolveEffective(preference: ThemePreference, systemPrefersDark: boolean): EffectiveTheme {
    if (preference === 'system') {
      return systemPrefersDark ? 'dark' : 'light';
    }
    return preference;
  }

  private applyToBody(theme: EffectiveTheme): void {
    if (typeof document === 'undefined') {
      return;
    }
    document.body.classList.remove('light-theme', 'dark-theme');
    document.body.classList.add(`${theme}-theme`);
  }
}
