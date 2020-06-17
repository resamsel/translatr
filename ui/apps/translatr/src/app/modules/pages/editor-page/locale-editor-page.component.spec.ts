import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';
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

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocaleEditorPageComponent],
      imports: [
        RouterTestingModule,
        EditorTestingModule,
        FilterFieldTestingModule,
        NavListTestingModule,
        EmptyViewTestingModule,

        NoopAnimationsModule,
        TranslocoTestingModule,

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
            unloadEditor$: mockObservable()
          })
        },
        {
          provide: ProjectFacade,
          useFactory: () => ({})
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleEditorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
