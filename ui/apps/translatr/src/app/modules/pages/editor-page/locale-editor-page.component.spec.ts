import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { readFileSync } from 'fs';
import { join } from 'path';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { EmptyViewTestingModule, FilterFieldTestingModule } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';
import { NavListTestingModule } from '../../shared/nav-list/testing';
import { ProjectFacade } from '../../shared/project-state/+state';
import { EditorFacade } from './+state/editor.facade';
import { EditorTestingModule } from './editor/testing';
import { LocaleEditorPageComponent } from './locale-editor-page.component';

describe('LocaleEditorPageComponent', () => {
  let component: LocaleEditorPageComponent;
  let fixture: ComponentFixture<LocaleEditorPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [LocaleEditorPageComponent],
        imports: [
          RouterTestingModule,
          EditorTestingModule,
          FilterFieldTestingModule,
          NavListTestingModule,
          EmptyViewTestingModule,

          NoopAnimationsModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatFormFieldModule,
          MatInputModule,
          MatIconModule,
          MatMenuModule,
          MatDialogModule
        ],
        providers: [
          {
            provide: AppFacade,
            useFactory: () => ({
              me$: mockObservable(),
              routeParams$: mockObservable(),
              queryParams$: mockObservable()
            })
          },
          {
            provide: EditorFacade,
            useFactory: () => ({
              selectedKeyName$: mockObservable(),
              localeSelectedMessage$: mockObservable(),
              unloadEditor$: mockObservable(),
              unloadEditor: jest.fn()
            })
          },
          {
            provide: ProjectFacade,
            useFactory: () => ({})
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleEditorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders its search field with an outline appearance, matching the rest of the UI', () => {
    // The `dev-filter-field` lives inside `<app-editor>`'s projected content, which the
    // EditorTestingModule stub (empty template, no <ng-content>) never renders into the DOM -
    // so this reads the real template source instead of querying the rendered fixture.
    const template = readFileSync(join(__dirname, 'locale-editor-page.component.html'), 'utf8');
    const filterFieldMarkup = template.match(/<dev-filter-field[\s\S]*?>/)[0];
    expect(filterFieldMarkup).toContain('appearance="outline"');
  });
});
