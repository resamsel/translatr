import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocaleEditorPageComponent } from './locale-editor-page.component';

describe('LocaleEditorPageComponent', () => {
  let component: LocaleEditorPageComponent;
  let fixture: ComponentFixture<LocaleEditorPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocaleEditorPageComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleEditorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
