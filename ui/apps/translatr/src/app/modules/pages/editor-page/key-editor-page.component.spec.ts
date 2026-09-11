import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { readFileSync } from 'fs';
import { join } from 'path';
import { MatDialogModule } from '@angular/material/dialog';
import { ProjectFacade } from '../../shared/project-state/+state';
import { KeyEditorPageComponent } from './key-editor-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../../../+state/app.facade';
import { EditorFacade } from './+state/editor.facade';
import { EditorTestingModule } from './editor/testing';
import { EmptyViewTestingModule, FilterFieldTestingModule } from '@translatr/components/testing';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { NavListTestingModule } from '../../testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { mockObservable } from '@translatr/utils/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('KeyEditorPageComponent', () => {
  let component: KeyEditorPageComponent;
  let fixture: ComponentFixture<KeyEditorPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [KeyEditorPageComponent],
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
              selectedLocaleName$: mockObservable(),
              keySelectedMessage$: mockObservable(),
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
    fixture = TestBed.createComponent(KeyEditorPageComponent);
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
    const template = readFileSync(join(__dirname, 'key-editor-page.component.html'), 'utf8');
    const filterFieldMarkup = template.match(/<dev-filter-field[\s\S]*?>/)[0];
    expect(filterFieldMarkup).toContain('appearance="outline"');
  });

  it('renders its selected-key field with an outline appearance, matching the rest of the UI', () => {
    // Same projected-content limitation as above: `.selector` lives inside
    // `<app-editor-selector>`, also stubbed with an empty template.
    const template = readFileSync(join(__dirname, 'key-editor-page.component.html'), 'utf8');
    const selectorMarkup = template.match(/<mat-form-field class="selector"[\s\S]*?>/)[0];
    expect(selectorMarkup).toContain('appearance="outline"');
  });
});
