import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessTokenEditDialogComponent } from './access-token-edit-dialog.component';

describe('ProjectCreationDialogComponent', () => {
  let component: AccessTokenEditDialogComponent;
  let fixture: ComponentFixture<AccessTokenEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AccessTokenEditDialogComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokenEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
