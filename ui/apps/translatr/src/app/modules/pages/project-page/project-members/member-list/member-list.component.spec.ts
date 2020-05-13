import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MemberListComponent } from './member-list.component';
import { NavListTestingModule } from '../../../../shared/nav-list/testing';
import { ButtonTestingModule, EmptyViewTestingModule } from '@translatr/components/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatButtonModule, MatDialogModule, MatIconModule, MatListModule, MatProgressBarModule, MatTooltipModule } from '@angular/material';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { AppFacade } from '../../../../../+state/app.facade';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('MemberListComponent', () => {
  let component: MemberListComponent;
  let fixture: ComponentFixture<MemberListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MemberListComponent],
      imports: [
        NavListTestingModule,
        ButtonTestingModule,
        EmptyViewTestingModule,

        RouterTestingModule,
        TranslocoTestingModule,

        MatDialogModule,
        MatListModule,
        MatIconModule,
        MatProgressBarModule,
        MatTooltipModule,
        MatButtonModule,

        GravatarModule,
        MomentModule
      ],
      providers: [
        { provide: AppFacade, useFactory: () => ({}) }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MemberListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
