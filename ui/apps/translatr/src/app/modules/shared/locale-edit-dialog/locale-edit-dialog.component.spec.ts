import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { LocaleEditDialogComponent } from './locale-edit-dialog.component';

describe('LocaleCreationDialogComponent', () => {
  let component: LocaleEditDialogComponent;
  let fixture: ComponentFixture<LocaleEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocaleEditDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
