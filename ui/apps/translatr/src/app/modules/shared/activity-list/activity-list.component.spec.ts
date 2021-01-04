import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EmptyViewTestingModule } from '@translatr/components/testing';
import { ActivityListComponent } from './activity-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MomentModule } from 'ngx-moment';
import { GravatarModule } from 'ngx-gravatar';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivityProjectLinkComponent } from './activity-project-link/activity-project-link.component';
import { ActivityLocaleLinkComponent } from './activity-locale-link/activity-locale-link.component';
import { ActivityKeyLinkComponent } from './activity-key-link/activity-key-link.component';
import { ActivityMemberLinkComponent } from './activity-member-link/activity-member-link.component';
import { ActivityMessageLinkComponent } from './activity-message-link/activity-message-link.component';
import { ActivityAccessTokenLinkComponent } from './activity-access-token-link/activity-access-token-link.component';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { TagModule } from '@dev/translatr-components';

describe('ActivityListComponent', () => {
  let component: ActivityListComponent;
  let fixture: ComponentFixture<ActivityListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [
          ActivityListComponent,
          ActivityProjectLinkComponent,
          ActivityLocaleLinkComponent,
          ActivityKeyLinkComponent,
          ActivityMemberLinkComponent,
          ActivityMessageLinkComponent,
          ActivityAccessTokenLinkComponent
        ],
        imports: [
          NavListTestingModule,

          RouterTestingModule,
          TranslocoTestingModule,
          EmptyViewTestingModule,

          TagModule,

          MatListModule,
          MatIconModule,
          MatChipsModule,
          MatTooltipModule,

          MomentModule,
          GravatarModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
