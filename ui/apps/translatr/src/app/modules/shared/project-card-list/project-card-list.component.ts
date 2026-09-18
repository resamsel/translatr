import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { TimeAgoPipe } from '@dev/translatr-components';
import { PagedList, Project } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';
import { trackByFn } from '@translatr/utils';
import { NavListComponent } from '../nav-list/nav-list.component';
import { ProjectCardComponent } from '../project-card/project-card.component';
import { ProjectCardLinkComponent } from '../project-card/project-card-link.component';
import { ProjectEmptyViewComponent } from '../project-empty-view/project-empty-view.component';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card-list',
  templateUrl: './project-card-list.component.html',
  styleUrls: ['./project-card-list.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    NavListComponent,
    MatButtonModule,
    TimeAgoPipe,
    ProjectCardComponent,
    ProjectCardLinkComponent,
    ProjectEmptyViewComponent
  ]
})
export class ProjectCardListComponent {
  @Input() projects: PagedList<Project>;
  @Input() canCreate = false;
  @Input() showMore = true;
  @Input() showMoreLink: any[] | string | null | undefined;

  @Output() readonly create = new EventEmitter<void>();

  firstChar = firstChar;
  trackByFn = trackByFn;

  onCreateProject(): void {
    this.create.emit();
  }
}
