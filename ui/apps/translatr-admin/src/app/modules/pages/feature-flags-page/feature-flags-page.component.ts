import { ChangeDetectionStrategy, Component } from '@angular/core';

interface FeatureFlagsTab {
  path: string;
  icon: string;
  /** Transloco key for the tab label. */
  name: string;
}

/**
 * Shell for the two feature-flag views, presented as tabs in the page header
 * (mirroring the project page): "User" (the current user's per-user overrides)
 * and "Global" (application-wide flags). Each tab is a child route rendered into
 * the {@code mat-tab-nav-panel} below.
 */
@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-feature-flags-page',
  templateUrl: './feature-flags-page.component.html',
  styleUrls: ['./feature-flags-page.component.scss']
})
export class FeatureFlagsPageComponent {
  readonly children: FeatureFlagsTab[] = [
    { path: 'user', icon: 'person', name: 'featureFlags.tab.user' },
    { path: 'global', icon: 'public', name: 'featureFlags.tab.global' }
  ];
}
