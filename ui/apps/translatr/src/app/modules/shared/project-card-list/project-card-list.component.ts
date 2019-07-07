import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PagedList, Project } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card-list',
  templateUrl: './project-card-list.component.html',
  styleUrls: ['./project-card-list.component.scss']
})
export class ProjectCardListComponent {
  @Input() projects: PagedList<Project>;
  @Input() allowProjectCreation = false;
  @Input() showMore = true;

  @Output() onCreate = new EventEmitter<void>();

  firstChar = firstChar;

  onCreateProject(): void {
    this.onCreate.emit();
  }
}
