import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Project } from '@dev/translatr-model';
import { ProjectCardComponent } from './project-card.component';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card-link',
  templateUrl: './project-card-link.component.html',
  styleUrls: ['./project-card-link.component.scss'],
  imports: [RouterModule, ProjectCardComponent]
})
export class ProjectCardLinkComponent {
  @Input() project: Project;
}
