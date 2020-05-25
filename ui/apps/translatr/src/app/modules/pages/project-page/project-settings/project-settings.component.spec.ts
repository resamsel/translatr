import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectSettingsComponent } from './project-settings.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectFacade } from '../+state/project.facade';
import { ProjectService } from '@dev/translatr-sdk';
import { RouterTestingModule } from '@angular/router/testing';
import { mockObservable } from '@translatr/utils/testing';
import { EmptyViewTestingModule } from '@translatr/components/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { AppFacade } from '../../../../+state/app.facade';

describe('ProjectSettingsComponent', () => {
  let component: ProjectSettingsComponent;
  let fixture: ComponentFixture<ProjectSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectSettingsComponent],
      imports: [
        EmptyViewTestingModule,

        FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        MatDialogModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule
      ],
      providers: [
        {provide: MatSnackBar, useFactory: () => ({})},
        {
          provide: ProjectFacade,
          useFactory: () => ({
            canDelete$: mockObservable()
          })
        },
        {
          provide: AppFacade,
          useFactory: () => ({
            project$: mockObservable(),
            projectModified$: mockObservable()
          })
        },
        {provide: ProjectService, useFactory: () => ({})}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
