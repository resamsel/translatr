import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchFieldPageComponent } from './search-field-page.component';

describe('SearchFieldPageComponent', () => {
  let component: SearchFieldPageComponent;
  let fixture: ComponentFixture<SearchFieldPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SearchFieldPageComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchFieldPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
