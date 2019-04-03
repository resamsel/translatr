import { Component, OnInit } from '@angular/core';
import { User } from "../../../../shared/user";
import { Observable } from "rxjs";
import { PagedList } from "../../../../shared/paged-list";
import { ActivatedRoute } from "@angular/router";
import { UserService } from "../../../../services/user.service";
import { Aggregate } from "../../../../shared/aggregate";
import {Project} from "../../../../shared/project";
import {ProjectService} from "../../../../services/project.service";

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent implements OnInit {

  user: User;
  projects$: Observable<PagedList<Project> | undefined>;
  activity$: Observable<PagedList<Aggregate> | undefined>;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly userService: UserService,
    private readonly projectService: ProjectService) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
        this.projects$ = this.projectService.getProjects({
          params: {
            owner: this.user.username,
            order: 'whenUpdated desc'
          }});
        this.activity$ = this.userService.activity(data.user.id);
      });
  }
}
