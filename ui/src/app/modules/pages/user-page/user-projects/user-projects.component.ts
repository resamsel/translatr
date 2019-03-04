import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { User } from "../../../../shared/user";
import { ProjectService } from "../../../../services/project.service";
import { Observable } from "rxjs";
import { PagedList } from "../../../../shared/paged-list";
import { Project } from "../../../../shared/project";

@Component({
  selector: 'app-user-projects',
  templateUrl: './user-projects.component.html',
  styleUrls: ['./user-projects.component.scss']
})
export class UserProjectsComponent implements OnInit {

  user: User;
  projects$: Observable<PagedList<Project>>;

  constructor(private readonly route: ActivatedRoute, private readonly projectService: ProjectService) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { user: User }) => {
        console.log('parent.data', data);
        this.user = data.user;
        this.projects$ = this.projectService.getProjects(data.user.username);
      });
  }
}
