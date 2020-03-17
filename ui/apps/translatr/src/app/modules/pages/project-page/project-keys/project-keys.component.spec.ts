import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectKeysComponent } from './project-keys.component';
import { ProjectFacade } from '../+state/project.facade';
import { MatSnackBar } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';
import { mockObservable } from '@translatr/utils/testing';
import { KeyListTestingModule } from './key-list/testing';

describe('ProjectKeysComponent', () => {
  let component: ProjectKeysComponent;
  let fixture: ComponentFixture<ProjectKeysComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectKeysComponent],
      imports: [
        KeyListTestingModule,

        RouterTestingModule
      ],
      providers: [
        {
          provide: ProjectFacade,
          useFactory: () => ({
            project$: mockObservable(),
            keysCriteria$: mockObservable(),
            unload$: mockObservable()
          })
        },
        { provide: MatSnackBar, useFactory: () => ({}) }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectKeysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
