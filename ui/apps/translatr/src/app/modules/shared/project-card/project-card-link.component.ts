import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Project } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-card-link',
  templateUrl: './project-card-link.component.html',
  styleUrls: ['./project-card-link.component.scss']
})
export class ProjectCardLinkComponent implements OnInit {
  @Input() project: Project;

  constructor() {}

  ngOnInit() {}
}
