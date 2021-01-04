import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EmptyViewContentComponent } from './empty-view-content.component';

describe('EmptyViewContentComponent', () => {
  let component: EmptyViewContentComponent;
  let fixture: ComponentFixture<EmptyViewContentComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [EmptyViewContentComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyViewContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
