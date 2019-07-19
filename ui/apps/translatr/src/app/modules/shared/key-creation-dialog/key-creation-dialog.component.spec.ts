import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyCreationDialogComponent } from './key-creation-dialog.component';

describe('KeyCreationDialogComponent', () => {
  let component: KeyCreationDialogComponent;
  let fixture: ComponentFixture<KeyCreationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [KeyCreationDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyCreationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
