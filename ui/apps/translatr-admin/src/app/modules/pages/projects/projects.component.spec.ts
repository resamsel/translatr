import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { ConfirmButtonComponent, EntityTableComponent, SelectionActionsComponent } from '@dev/translatr-components';
import {
  MockConfirmButtonComponent,
  MockEntityTableComponent, MockSelectionActionsComponent
} from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';
import { AdminPageComponent } from '../../admin-page/admin-page.component';
import { MockAdminPageComponent } from '../../admin-page/testing';

import { ProjectsComponent } from './projects.component';

describe('ProjectsComponent', () => {
  let component: ProjectsComponent;
  let fixture: ComponentFixture<ProjectsComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectsComponent, {
        remove: {
          imports: [AdminPageComponent, EntityTableComponent, ConfirmButtonComponent, SelectionActionsComponent]
        },
        add: {
          imports: [MockAdminPageComponent, MockEntityTableComponent, MockConfirmButtonComponent, MockSelectionActionsComponent]
        }
      }).configureTestingModule({
        imports: [
          ProjectsComponent,

          RouterTestingModule,

          MatTableModule,
          MatButtonModule,
          MatTooltipModule,
          MatIconModule
        ],
        providers: [
          {
            provide: AppFacade,
            useFactory: () => ({
              me$: mockObservable(),
              projectDeleted$: mockObservable(),
              projectsDeleted$: mockObservable(),
              unloadProjects$: mockObservable(),
              unloadProjects: jest.fn()
            })
          },
          {
            provide: MatSnackBar,
            useFactory: () => ({})
          },
          {
            provide: MatDialog,
            useFactory: () => ({})
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
