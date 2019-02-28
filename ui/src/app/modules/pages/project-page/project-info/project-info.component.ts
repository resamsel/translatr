import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Project} from "../../../../shared/project";
import {ActivatedRoute} from "@angular/router";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-info',
  templateUrl: './project-info.component.html',
  styleUrls: ['./project-info.component.scss']
})
export class ProjectInfoComponent implements OnInit {

  project: Project;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: {project: Project}) => {
        this.project = data.project;
      });
  }
}
