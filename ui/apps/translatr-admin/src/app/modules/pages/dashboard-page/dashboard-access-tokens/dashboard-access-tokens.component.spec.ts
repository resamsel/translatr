import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardAccessTokensComponent } from './dashboard-access-tokens.component';

describe('DashboardAccessTokensComponent', () => {
  let component: DashboardAccessTokensComponent;
  let fixture: ComponentFixture<DashboardAccessTokensComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DashboardAccessTokensComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardAccessTokensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
