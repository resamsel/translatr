import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EmptyViewHeaderComponent } from './empty-view-header.component';

describe('EmptyViewHeaderComponent', () => {
  let component: EmptyViewHeaderComponent;
  let fixture: ComponentFixture<EmptyViewHeaderComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [EmptyViewHeaderComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyViewHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
