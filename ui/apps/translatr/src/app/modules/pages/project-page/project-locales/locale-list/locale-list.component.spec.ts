import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LocaleListComponent } from './locale-list.component';
import {
  ConfirmButtonComponent,
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent
} from '@dev/translatr-components';
import { NavListComponent } from '../../../../shared/nav-list/nav-list.component';
import { MockNavListComponent } from '../../../../shared/nav-list/testing';
import { MockConfirmButtonComponent, MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent } from '@translatr/components/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('LocaleListComponent', () => {
  let component: LocaleListComponent;
  let fixture: ComponentFixture<LocaleListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(LocaleListComponent, {
        remove: {
          imports: [
            NavListComponent,
            ConfirmButtonComponent,
            EmptyViewComponent,
            EmptyViewHeaderComponent,
            EmptyViewContentComponent,
            EmptyViewActionsComponent
          ]
        },
        add: {
          imports: [
            MockNavListComponent,
            MockConfirmButtonComponent,
            MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent
          ]
        }
      }).configureTestingModule({
        imports: [
          LocaleListComponent,
          RouterTestingModule,
          MatDialogModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
