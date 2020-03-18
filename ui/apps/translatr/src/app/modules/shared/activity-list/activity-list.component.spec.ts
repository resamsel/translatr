import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivityListComponent } from './activity-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { MatTooltipModule } from '@angular/material';
import { TagModule } from '@translatr/translatr-components/src/lib/modules/tag/tag.module';
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

describe('ActivityListComponent', () => {
  let component: ActivityListComponent;
  let fixture: ComponentFixture<ActivityListComponent>;

  beforeEach(async(() => {
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

        TagModule,

        MatListModule,
        MatIconModule,
        MatChipsModule,
        MatTooltipModule,

        MomentModule,
        GravatarModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
