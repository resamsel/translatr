import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { HotkeysService } from '@ngneat/hotkeys';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { EmptyViewTestingModule } from '@translatr/components/testing';
import { EditorFacade } from '../+state/editor.facade';
import { SidenavTestingModule } from '../../../nav/sidenav/testing';
import { EMPTY, Subject } from 'rxjs';
import { EditorComponent } from './editor.component';

const editorTestImports = [
  SidenavTestingModule,

  RouterTestingModule,
  FormsModule,
  NoopAnimationsModule,
  TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),
  EmptyViewTestingModule,

  MatButtonModule,
  MatDividerModule,
  MatTabsModule,
  MatCardModule,
  MatIconModule,
  MatMenuModule,
  MatSnackBarModule,

  CodemirrorModule
];

describe('EditorComponent', () => {
  let component: EditorComponent;
  let fixture: ComponentFixture<EditorComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [EditorComponent],
        imports: editorTestImports,
        providers: [
          {
            provide: EditorFacade,
            useFactory: () => ({})
          },
          {
            provide: HotkeysService,
            useValue: {
              addShortcut: jest.fn().mockReturnValue(EMPTY),
              removeShortcuts: jest.fn(),
              registerHelpModal: jest.fn()
            }
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(EditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

describe('EditorComponent save shortcut', () => {
  let fixture: ComponentFixture<EditorComponent>;
  let facade: { saveMessage: jest.Mock; message$: Subject<unknown>; saveBehavior$: Subject<unknown> };
  let originalUserAgent: string;

  beforeEach(
    waitForAsync(() => {
      originalUserAgent = navigator.userAgent;
      // The save shortcut is meant to be Cmd+Enter on macOS / Ctrl+Enter elsewhere.
      // @ngneat/hotkeys resolves the platform from the user agent, so pretend we are on a Mac.
      Object.defineProperty(navigator, 'userAgent', {
        value: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
        configurable: true
      });

      facade = {
        saveMessage: jest.fn(),
        message$: new Subject(),
        saveBehavior$: new Subject()
      };

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        declarations: [EditorComponent],
        imports: editorTestImports,
        providers: [
          { provide: EditorFacade, useValue: facade },
          HotkeysService
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(EditorComponent);
    fixture.detectChanges();
  });

  afterEach(() => {
    Object.defineProperty(navigator, 'userAgent', { value: originalUserAgent, configurable: true });
  });

  it('saves the translation on Cmd+Enter (macOS)', () => {
    document.documentElement.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', metaKey: true, bubbles: true })
    );

    expect(facade.saveMessage).toHaveBeenCalled();
  });

  it('saves and advances to the next item on Cmd+Shift+Enter (macOS)', () => {
    const nextItem = jest.spyOn(fixture.componentInstance.nextItem, 'emit');

    document.documentElement.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', metaKey: true, shiftKey: true, bubbles: true })
    );
    // onSave() skips the current message$ value and waits for the save to land
    facade.message$.next({});
    facade.message$.next({});

    expect(facade.saveMessage).toHaveBeenCalled();
    expect(nextItem).toHaveBeenCalled();
  });
});
