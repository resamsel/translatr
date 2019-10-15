import { Component, Input } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { TitleService } from '@translatr/utils/src/lib/services/title.service';

export interface Link {
  routerLink: string[];
  name?: string;
}

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  @Input() title = 'Translatr';
  @Input() sidenav: MatDrawer;
  @Input() backLink: Link;

  constructor(private readonly titleService: TitleService) {
  }

  private _page: string;

  get page(): string {
    return this._page;
  }

  @Input() elevated = true;

  @Input() set page(page: string) {
    this.titleService.setTitle(page);
    this._page = page;
  }
}
