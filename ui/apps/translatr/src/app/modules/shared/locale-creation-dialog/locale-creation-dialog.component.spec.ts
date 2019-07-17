import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { LocaleCreationDialogComponent } from './locale-creation-dialog.component';

describe('LocaleCreationDialogComponent', () => {
  let component: LocaleCreationDialogComponent;
  let fixture: ComponentFixture<LocaleCreationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocaleCreationDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleCreationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
