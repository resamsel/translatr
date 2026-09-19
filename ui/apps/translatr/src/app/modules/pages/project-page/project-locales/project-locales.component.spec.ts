import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../../../shared/project-state';
import { LocaleListComponent } from './locale-list/locale-list.component';
import { LocaleListTestingModule } from './locale-list/testing';
import { ProjectLocalesComponent } from './project-locales.component';

describe('ProjectLocalesComponent', () => {
  let component: ProjectLocalesComponent;
  let fixture: ComponentFixture<ProjectLocalesComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectLocalesComponent, {
        remove: { imports: [LocaleListComponent] },
        add: { imports: [LocaleListTestingModule] }
      }).configureTestingModule({
        imports: [ProjectLocalesComponent, RouterTestingModule],
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
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectLocalesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
