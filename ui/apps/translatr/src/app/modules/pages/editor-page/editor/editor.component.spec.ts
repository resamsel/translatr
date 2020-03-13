import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { EditorComponent } from './editor.component';
import { EditorFacade } from '../+state/editor.facade';
import { SidenavTestingModule } from '../../../nav/sidenav/testing';
import { MatButtonModule, MatCardModule, MatDividerModule, MatIconModule, MatTabsModule } from '@angular/material';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

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

        MatButtonModule,
        MatDividerModule,
        MatTabsModule,
        MatCardModule,
        MatIconModule,

        CodemirrorModule
      ],
      providers: [
        {
          provide: EditorFacade, useFactory: () => ({})
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
