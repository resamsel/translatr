import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../../../shared/project-state';
import { LocaleListTestingModule } from './locale-list/testing';
import { ProjectLocalesComponent } from './project-locales.component';

describe('ProjectLocalesComponent', () => {
  let component: ProjectLocalesComponent;
  let fixture: ComponentFixture<ProjectLocalesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectLocalesComponent],
      imports: [LocaleListTestingModule, RouterTestingModule],
      providers: [
        {
          provide: ProjectFacade,
          useFactory: () => ({
            project$: mockObservable(),
            localesCriteria$: mockObservable(),
            unload$: mockObservable()
          })
        },
        { provide: MatSnackBar, useFactory: () => ({}) }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectLocalesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
