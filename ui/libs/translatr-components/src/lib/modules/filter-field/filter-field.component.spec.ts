import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterFieldComponent } from './filter-field.component';
import { Renderer2 } from '@angular/core';
import {
  MatAutocompleteModule,
  MatChipsModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatOptionModule,
  MatTooltipModule
} from '@angular/material';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('FilterFieldComponent', () => {
  let component: FilterFieldComponent;
  let fixture: ComponentFixture<FilterFieldComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FilterFieldComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        MatFormFieldModule,
        MatChipsModule,
        MatTooltipModule,
        MatIconModule,
        MatInputModule,
        MatAutocompleteModule,
        MatOptionModule
      ],
      providers: [
        { provide: Renderer2, useFactory: () => ({}) }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
