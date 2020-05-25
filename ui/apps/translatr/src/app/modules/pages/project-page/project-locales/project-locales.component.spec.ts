import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectLocalesComponent } from './project-locales.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ProjectFacade } from '../+state/project.facade';
import { mockObservable } from '@translatr/utils/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LocaleListTestingModule } from './locale-list/testing';

describe('ProjectLocalesComponent', () => {
  let component: ProjectLocalesComponent;
  let fixture: ComponentFixture<ProjectLocalesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectLocalesComponent],
      imports: [
        LocaleListTestingModule,

        RouterTestingModule
      ],
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
