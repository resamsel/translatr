import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyViewHeaderComponent } from './empty-view-header.component';

describe('EmptyViewHeaderComponent', () => {
  let component: EmptyViewHeaderComponent;
  let fixture: ComponentFixture<EmptyViewHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EmptyViewHeaderComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyViewHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
