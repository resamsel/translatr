import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { ConfirmButtonComponent } from '@dev/translatr-components';
import { MockConfirmButtonComponent } from '@translatr/components/testing';
import { AppFacade } from '../../../../../+state/app.facade';
import { NavListComponent } from '../../../../shared/nav-list/nav-list.component';
import { MockNavListComponent } from '../../../../shared/nav-list/testing';

import { MemberListComponent } from './member-list.component';

describe('MemberListComponent', () => {
  let component: MemberListComponent;
  let fixture: ComponentFixture<MemberListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(MemberListComponent, {
        remove: { imports: [NavListComponent, ConfirmButtonComponent] },
        add: { imports: [MockNavListComponent, MockConfirmButtonComponent] }
      }).configureTestingModule({
        imports: [
          MemberListComponent,
          RouterTestingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),
          MatDialogModule
        ],
        providers: [{ provide: AppFacade, useFactory: () => ({}) }]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(MemberListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
