import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SelectionActionsComponent } from './selection-actions.component';

describe('SelectionActionsComponent', () => {
  let component: SelectionActionsComponent;
  let fixture: ComponentFixture<SelectionActionsComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [SelectionActionsComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectionActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
