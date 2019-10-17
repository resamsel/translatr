import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { Project } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card',
  templateUrl: './project-card.component.html',
  styleUrls: ['./project-card.component.scss']
})
export class ProjectCardComponent {
  constructor() {}

  @Input() project: Project;

  firstChar = firstChar;

  @HostBinding('class') clazz = 'project-card';
}
