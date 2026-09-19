import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { KeyListComponent } from './key-list.component';
import { RouterTestingModule } from '@angular/router/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@jsverse/transloco';
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

describe('KeyListComponent', () => {
  let component: KeyListComponent;
  let fixture: ComponentFixture<KeyListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(KeyListComponent, {
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
          KeyListComponent,
          RouterTestingModule,
          MatDialogModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
