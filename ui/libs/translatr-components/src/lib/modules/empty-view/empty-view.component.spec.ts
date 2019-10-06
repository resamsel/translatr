import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyViewComponent } from './empty-view.component';

describe('EmptyViewComponent', () => {
  let component: EmptyViewComponent;
  let fixture: ComponentFixture<EmptyViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EmptyViewComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
