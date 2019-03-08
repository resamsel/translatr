import { Component, OnInit } from '@angular/core';
import {Project} from "../../../../shared/project";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-project-locales',
  templateUrl: './project-locales.component.html',
  styleUrls: ['./project-locales.component.scss']
})
export class ProjectLocalesComponent implements OnInit {

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
