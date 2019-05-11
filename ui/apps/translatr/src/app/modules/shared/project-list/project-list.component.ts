import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { PagedList } from "../../../../../../../libs/translatr-model/src/lib/model/paged-list";
import { Project } from "../../../../../../../libs/translatr-model/src/lib/model/project";
import { firstChar } from "../../../../../../../libs/translatr-sdk/src/lib/shared/utils";

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
