import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Project } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';
import { TimeAgoPipe } from '@dev/translatr-components';
import { GravatarModule } from 'ngx-gravatar';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card',
  templateUrl: './project-card.component.html',
  styleUrls: ['./project-card.component.scss'],
  imports: [CommonModule, MatCardModule, MatIconModule, GravatarModule, TimeAgoPipe, MatTooltipModule]
})
export class ProjectCardComponent {
  @Input() project: Project;

  firstChar = firstChar;

  @HostBinding('class') clazz = 'project-card';
}
