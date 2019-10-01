import { Component, Input } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User } from '@dev/translatr-model';
import { environment } from '../../../../environments/environment';
import { MatDrawer } from '@angular/material/sidenav';
import { Link } from '@dev/translatr-components';

@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss']
})
export class SidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() sidenav: MatDrawer;

  readonly endpointUrl = environment.endpointUrl;

  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(map(result => result.matches));

  constructor(private breakpointObserver: BreakpointObserver) {
  }

  uiRoute(): string {
    return environment.uiUrl;
  }
}
