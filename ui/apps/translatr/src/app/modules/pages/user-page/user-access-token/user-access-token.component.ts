import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { ActivatedRoute } from '@angular/router';
import { filter, take } from 'rxjs/operators';
import { UserFacade } from '../+state/user.facade';
import { AccessTokenEditFormComponent } from '../../../shared/access-token-edit-form/access-token-edit-form.component';

@Component({
  standalone: true,
  selector: 'app-user-access-token',
  templateUrl: './user-access-token.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./user-access-token.component.scss'],
  imports: [CommonModule, MatCardModule, AccessTokenEditFormComponent]
})
export class UserAccessTokenComponent implements OnInit {
  accessToken$ = this.facade.accessToken$.pipe(filter(x => !!x));

  constructor(private readonly route: ActivatedRoute, private readonly facade: UserFacade) {}

  ngOnInit() {
    this.route.params.pipe(take(1)).subscribe(params => this.facade.loadAccessToken(params.id));
  }
}
