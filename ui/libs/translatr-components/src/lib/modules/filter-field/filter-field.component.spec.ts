import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterFieldComponent } from './filter-field.component';
import { Renderer2 } from '@angular/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { MatOptionModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
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
