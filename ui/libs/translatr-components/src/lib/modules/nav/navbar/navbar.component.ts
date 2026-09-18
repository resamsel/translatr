import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostBinding, Input, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDrawer } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TitleService } from '@translatr/utils';

export interface Link {
  routerLink: string[];
  name?: string;
}

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  imports: [CommonModule, RouterModule, MatToolbarModule, MatButtonModule, MatIconModule, MatTooltipModule]
})
export class NavbarComponent implements OnInit {
  @Input() title = 'Translatr';
  @Input() page: string;
  @Input() backLink: Link;
  @Input() sidenav: MatDrawer;
  @Input() elevated = true;
  @Input() showLogo = true;
  @Input() backgroundColor : string;
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
