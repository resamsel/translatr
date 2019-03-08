import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {PagedList} from "../../../shared/paged-list";
import {Project} from "../../../shared/project";

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectListComponent {
  @Input() projects: PagedList<Project>;
}
