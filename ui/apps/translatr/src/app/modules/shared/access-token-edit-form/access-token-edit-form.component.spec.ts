import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessTokenEditFormComponent } from './access-token-edit-form.component';

describe('AccessTokenEditFormComponent', () => {
  let component: AccessTokenEditFormComponent;
  let fixture: ComponentFixture<AccessTokenEditFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccessTokenEditFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokenEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
