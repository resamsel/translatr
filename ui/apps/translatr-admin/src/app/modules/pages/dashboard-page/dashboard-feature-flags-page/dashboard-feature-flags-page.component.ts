import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Shell for the two feature-flag views, presented as tabs: "User" (the current
 * user's per-user overrides) and "Global" (application-wide flags). Each tab's
 * content is lazily instantiated via {@code *matTabContent}.
 */
@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-feature-flags-page',
  templateUrl: './dashboard-feature-flags-page.component.html',
  styleUrls: ['./dashboard-feature-flags-page.component.scss']
})
export class DashboardFeatureFlagsPageComponent {}
