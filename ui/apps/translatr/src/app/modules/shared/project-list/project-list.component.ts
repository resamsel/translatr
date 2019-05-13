import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {PagedList, Project} from '@dev/translatr-model';
import {firstChar} from '@dev/translatr-sdk';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectListComponent {
  @Input() projects: PagedList<Project>;

  firstChar = firstChar;
}
