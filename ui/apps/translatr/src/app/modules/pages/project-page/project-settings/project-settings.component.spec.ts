import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectSettingsComponent } from './project-settings.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBar } from '@angular/material';
import { ProjectFacade } from '../+state/project.facade';
import { ProjectService } from '@dev/translatr-sdk';
import { RouterTestingModule } from '@angular/router/testing';
import { mockObservable } from '@translatr/utils/testing';
import { EmptyViewTestingModule } from '@translatr/components/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

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

        MatDialogModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule

      ],
      providers: [
        { provide: MatSnackBar, useFactory: () => ({}) },
        {
          provide: ProjectFacade,
          useFactory: () => ({
            project$: mockObservable()
          })
        },
        { provide: ProjectService, useFactory: () => ({}) }
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
