import { Component, OnInit } from '@angular/core';
import { User } from '../../../../../../../../libs/translatr-model/src/lib/model/user';
import { Observable } from 'rxjs';
import { PagedList } from '../../../../../../../../libs/translatr-model/src/lib/model/paged-list';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../../../../../../../libs/translatr-sdk/src/lib/services/user.service';
import { Aggregate } from '../../../../../../../../libs/translatr-model/src/lib/model/aggregate';
import { Project } from '../../../../../../../../libs/translatr-model/src/lib/model/project';
import { ProjectService } from '../../../../../../../../libs/translatr-sdk/src/lib/services/project.service';

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
        this.projects$ = this.projectService.find({
            owner: this.user.username,
            order: 'whenUpdated desc'
          });
        this.activity$ = this.userService.activity(data.user.id);
      });
  }
}
