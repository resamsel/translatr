import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
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
import { TranslocoTestingModule } from '@ngneat/transloco';

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
          TranslocoTestingModule,

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
              unloadEditor$: mockObservable()
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
});
