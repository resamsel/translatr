import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { Link } from '@dev/translatr-components';
import { Feature, User } from '@dev/translatr-model';
import { environment } from '../../../../environments/environment';

@Component({
  standalone: false,
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./sidenav.component.scss']
})
export class SidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() sidenav: MatDrawer;
  @Input() showFooter = true;
  @Input() overlay = false;

  readonly endpointUrl = environment.endpointUrl;
  readonly uiUrl = environment.uiUrl;

  readonly Feature = Feature;
}
