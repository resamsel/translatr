import {Component, OnInit} from '@angular/core';
import {Project} from "../../../shared/project";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-project-page',
  templateUrl: './project-page.component.html',
  styleUrls: ['./project-page.component.scss']
})
export class ProjectPageComponent implements OnInit {

  project: Project;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.data
      .subscribe((data: { project: Project }) => {
        this.project = data.project;
        console.log('page data', data);
      });
  }
}
