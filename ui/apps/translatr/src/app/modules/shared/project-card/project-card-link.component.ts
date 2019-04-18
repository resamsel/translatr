import {Component, Input, OnInit} from '@angular/core';
import {Project} from "../../../../../../../libs/translatr-model/src/lib/model/project";

@Component({
  selector: 'app-project-card-link',
  templateUrl: './project-card-link.component.html',
  styleUrls: ['./project-card-link.component.scss']
})
export class ProjectCardLinkComponent implements OnInit {

  @Input() project: Project;

  constructor() {
  }

  ngOnInit() {
  }

}
