import { async, ComponentFixture, TestBed } from '@angular/core/testing';
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
import { HotkeysModule } from '@ngneat/hotkeys';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { EditorFacade } from '../+state/editor.facade';
import { SidenavTestingModule } from '../../../nav/sidenav/testing';
import { EditorComponent } from './editor.component';

describe('EditorComponent', () => {
  let component: EditorComponent;
  let fixture: ComponentFixture<EditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EditorComponent],
      imports: [
        SidenavTestingModule,

        RouterTestingModule,
        FormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,
        HotkeysModule,

        MatButtonModule,
        MatDividerModule,
        MatTabsModule,
        MatCardModule,
        MatIconModule,
        MatMenuModule,
        MatSnackBarModule,

        CodemirrorModule
      ],
      providers: [
        {
          provide: EditorFacade,
          useFactory: () => ({})
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
