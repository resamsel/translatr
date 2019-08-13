import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TileLinkComponent } from './tile-link.component';

describe('TileLinkComponent', () => {
  let component: TileLinkComponent;
  let fixture: ComponentFixture<TileLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TileLinkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TileLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
