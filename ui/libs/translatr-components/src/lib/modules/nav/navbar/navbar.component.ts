import { ChangeDetectionStrategy, Component, HostBinding, Input, OnInit } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { TitleService } from '@translatr/utils';

export interface Link {
  routerLink: string[];
  name?: string;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  @Input() title = 'Translatr';
  @Input() page: string;
  @Input() backLink: Link;
  @Input() sidenav: MatDrawer;
  @Input() elevated = true;
  @Input() showLogo = true;
  @HostBinding('class.overlay') @Input() overlay = false;

  constructor(private readonly titleService: TitleService) {}

  ngOnInit(): void {
    if (this.page) {
      this.titleService.setTitle(this.page);
    } else if (this.backLink && this.backLink.name) {
      this.titleService.setTitle(this.backLink.name);
    } else {
      this.titleService.setTitle(undefined);
    }
  }
}
