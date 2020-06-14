import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../../../shared/project-state';
import { KeyListTestingModule } from './key-list/testing';

import { ProjectKeysComponent } from './project-keys.component';

describe('ProjectKeysComponent', () => {
  let component: ProjectKeysComponent;
  let fixture: ComponentFixture<ProjectKeysComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectKeysComponent],
      imports: [KeyListTestingModule, RouterTestingModule],
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
