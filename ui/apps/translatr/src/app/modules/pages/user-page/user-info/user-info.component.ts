import { Component, OnInit } from '@angular/core';
import { Aggregate, PagedList, Project, User } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { ProjectService, UserService } from '@dev/translatr-sdk';

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
    private readonly projectService: ProjectService
  ) {}

  ngOnInit() {
    this.route.parent.data.subscribe((data: { user: User }) => {
      this.user = data.user;
      this.projects$ = this.projectService.find({
        owner: this.user.username,
        order: 'whenUpdated desc'
      });
      this.activity$ = this.userService.activity(data.user.id);
    });
  }
}
