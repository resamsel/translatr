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

  @Output() readonly create = new EventEmitter<void>();

  firstChar = firstChar;
  trackByFn = trackByFn;

  constructor() {
    this.create.subscribe(() => console.log('ProjectCardList.create'));
  }

  onCreateProject(): void {
    console.log('onCreateProject');
    this.create.emit();
  }
}
