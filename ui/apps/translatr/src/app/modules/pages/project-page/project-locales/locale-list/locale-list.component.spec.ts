import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocaleListComponent } from './locale-list.component';
import { NavListTestingModule } from '../../../../shared/nav-list/testing';
import { ButtonTestingModule, EmptyViewTestingModule } from '@translatr/components/testing';
import { RouterTestingModule } from '@angular/router/testing';
import {
  MatButtonModule,
  MatDialogModule,
  MatIconModule,
  MatListModule,
  MatMenuModule,
  MatProgressBarModule,
  MatTooltipModule
} from '@angular/material';

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
