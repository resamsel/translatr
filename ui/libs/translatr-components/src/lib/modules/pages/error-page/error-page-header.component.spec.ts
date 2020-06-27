import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorPageHeaderComponent } from './error-page-header.component';

describe('ErrorPageHeaderComponent', () => {
  let component: ErrorPageHeaderComponent;
  let fixture: ComponentFixture<ErrorPageHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ErrorPageHeaderComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorPageHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
