import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectionActionsComponent } from './selection-actions.component';

describe('SelectionActionsComponent', () => {
  let component: SelectionActionsComponent;
  let fixture: ComponentFixture<SelectionActionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectionActionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectionActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
