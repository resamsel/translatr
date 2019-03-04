import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {User} from "../../../../shared/user";
import {ProjectService} from "../../../../services/project.service";
import {Observable} from "rxjs";
import {PagedList} from "../../../../shared/paged-list";
import {Project} from "../../../../shared/project";

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent implements OnInit {

  user: User;
  projects$: Observable<PagedList<Project>>;

  constructor(private readonly route: ActivatedRoute, private readonly projectService: ProjectService) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
      });
    this.projects$ = this.projectService.getProjects();
  }
}
