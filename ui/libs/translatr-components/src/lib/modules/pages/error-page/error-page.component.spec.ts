import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { ErrorPageHeaderComponent } from './error-page-header.component';
import { ErrorPageMessageComponent } from './error-page-message.component';

import { ErrorPageComponent } from './error-page.component';

describe('ErrorPageComponent', () => {
  let component: ErrorPageComponent;
  let fixture: ComponentFixture<ErrorPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent],
      imports: [MatIconModule]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
