import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { LocaleEditorPageComponent } from './locale-editor-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { EditorTestingModule } from './editor/testing';
import { FilterFieldTestingModule } from '@translatr/components/testing';
import { NavListTestingModule } from '../../shared/nav-list/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule } from '@angular/material';
import { AppFacade } from '../../../+state/app.facade';
import { EditorFacade } from './+state/editor.facade';
import { mockObservable } from '@translatr/utils/testing';

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

        NoopAnimationsModule,

        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatMenuModule
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
