import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyViewActionsComponent } from './empty-view-actions.component';

describe('EmptyViewActionsComponent', () => {
  let component: EmptyViewActionsComponent;
  let fixture: ComponentFixture<EmptyViewActionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EmptyViewActionsComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyViewActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
