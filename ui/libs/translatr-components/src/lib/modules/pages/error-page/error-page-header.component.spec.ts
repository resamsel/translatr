import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ErrorPageHeaderComponent } from './error-page-header.component';

describe('ErrorPageHeaderComponent', () => {
  let component: ErrorPageHeaderComponent;
  let fixture: ComponentFixture<ErrorPageHeaderComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ErrorPageHeaderComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorPageHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
