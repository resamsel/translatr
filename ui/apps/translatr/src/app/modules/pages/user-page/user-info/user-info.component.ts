import { Component, OnInit } from '@angular/core';
import { User } from '@dev/translatr-model';
import { ActivatedRoute } from '@angular/router';
import { ProjectService, UserService } from '@dev/translatr-sdk';
import { UserFacade } from '../+state/user.facade';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent implements OnInit {
  user$ = this.facade.user$;
  projects$ = this.facade.projects$;
  activities$ = this.facade.activities$;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly facade: UserFacade,
    private readonly userService: UserService,
    private readonly projectService: ProjectService
  ) {
  }

  ngOnInit() {
    this.facade.user$
      .pipe(filter(user => !!user))
      .subscribe((user: User) => {
        this.facade.loadProjects({
          owner: user.username,
          order: 'whenUpdated desc'
        });
        this.facade.loadActivities({
          userId: user.id
        });
      });
  }
}
