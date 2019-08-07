import { Component, Input } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User, UserRole } from '@dev/translatr-model';
import { environment } from '../../../../environments/environment';

export interface Link {
  routerLink: string[];
  name: string;
}

@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss']
})
export class SidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;

  readonly endpointUrl = environment.endpointUrl;

  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(map(result => result.matches));

  constructor(private breakpointObserver: BreakpointObserver) {
  }

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }

  adminRoute(): string {
    return environment.adminUrl;
  }
}
