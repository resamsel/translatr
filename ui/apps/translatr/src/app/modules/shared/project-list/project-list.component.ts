import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PagedList, Project } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent {
  @Input() projects: PagedList<Project>;
  @Input() allowProjectCreation = false;

  @Output() create = new EventEmitter<void>();

  firstChar = firstChar;

  onCreateProject(): void {
    this.create.emit();
  }
}
