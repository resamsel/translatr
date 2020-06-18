import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PagedList, Project } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';
import { trackByFn } from '@translatr/utils';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card-list',
  templateUrl: './project-card-list.component.html',
  styleUrls: ['./project-card-list.component.scss']
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
