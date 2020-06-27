import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocaleListComponent } from './locale-list.component';
import { NavListTestingModule } from '../../../../shared/nav-list/testing';
import { ButtonTestingModule, EmptyViewTestingModule } from '@translatr/components/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

describe('LocaleListComponent', () => {
  let component: LocaleListComponent;
  let fixture: ComponentFixture<LocaleListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocaleListComponent],
      imports: [
        NavListTestingModule,
        ButtonTestingModule,
        EmptyViewTestingModule,

        RouterTestingModule,

        MatDialogModule,
        MatListModule,
        MatIconModule,
        MatProgressBarModule,
        MatTooltipModule,
        MatButtonModule,
        MatMenuModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
