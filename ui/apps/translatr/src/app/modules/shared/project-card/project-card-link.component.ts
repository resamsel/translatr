import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Project } from '@dev/translatr-model';

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card-link',
  templateUrl: './project-card-link.component.html',
  styleUrls: ['./project-card-link.component.scss']
})
export class ProjectCardLinkComponent {
  @Input() project: Project;
}
