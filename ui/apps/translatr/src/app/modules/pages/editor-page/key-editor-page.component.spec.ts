import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyEditorPageComponent } from './key-editor-page.component';

describe('KeyEditorPageComponent', () => {
  let component: KeyEditorPageComponent;
  let fixture: ComponentFixture<KeyEditorPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ KeyEditorPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyEditorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
