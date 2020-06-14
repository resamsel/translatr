import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyViewContentComponent } from './empty-view-content.component';

describe('EmptyViewContentComponent', () => {
  let component: EmptyViewContentComponent;
  let fixture: ComponentFixture<EmptyViewContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EmptyViewContentComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyViewContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
