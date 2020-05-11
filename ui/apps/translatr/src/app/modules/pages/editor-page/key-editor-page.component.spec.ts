import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyEditorPageComponent } from './key-editor-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../../../+state/app.facade';
import { EditorFacade } from './+state/editor.facade';
import { EditorTestingModule } from './editor/testing';
import { FilterFieldTestingModule } from '@translatr/components/testing';
import { MatFormFieldModule, MatIconModule, MatInputModule } from '@angular/material';
import { NavListTestingModule } from '../../testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { mockObservable } from '@translatr/utils/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('KeyEditorPageComponent', () => {
  let component: KeyEditorPageComponent;
  let fixture: ComponentFixture<KeyEditorPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [KeyEditorPageComponent],
      imports: [
        RouterTestingModule,
        EditorTestingModule,
        FilterFieldTestingModule,
        NavListTestingModule,

        NoopAnimationsModule,
        TranslocoTestingModule,

        MatFormFieldModule,
        MatInputModule,
        MatIconModule
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
            unloadEditor$: mockObservable()
          })
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyEditorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
