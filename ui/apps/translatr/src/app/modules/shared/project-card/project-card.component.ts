import {Component, Input, OnInit} from '@angular/core';
import {Project} from "../../../../../../../libs/translatr-model/src/lib/model/project";
import {firstChar} from "../../../../../../../libs/translatr-sdk/src/lib/shared/utils";

@Component({
  selector: 'app-project-card',
  templateUrl: './project-card.component.html',
  styleUrls: ['./project-card.component.scss'],
  host: {
    class: 'project-card'
  }
})
export class ProjectCardComponent implements OnInit {

  @Input() project: Project;

  constructor() {
  }

  ngOnInit() {
  }

  firstChar = firstChar;
}
