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
  selector: 'dev-dashboard-feature-flags-page',
  templateUrl: './dashboard-feature-flags-page.component.html',
  styleUrls: ['./dashboard-feature-flags-page.component.scss']
})
export class DashboardFeatureFlagsPageComponent {
  readonly children: FeatureFlagsTab[] = [
    { path: 'user', icon: 'person', name: 'featureFlags.tab.user' },
    { path: 'global', icon: 'public', name: 'featureFlags.tab.global' }
  ];
}
