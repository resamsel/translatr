import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyEditDialogComponent } from './key-edit-dialog.component';

describe('KeyCreationDialogComponent', () => {
  let component: KeyEditDialogComponent;
  let fixture: ComponentFixture<KeyEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [KeyEditDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
