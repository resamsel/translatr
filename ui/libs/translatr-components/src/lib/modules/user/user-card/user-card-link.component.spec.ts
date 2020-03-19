import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { UserCardLinkComponent } from '@dev/translatr-components';
import { MockUserCardComponent } from '@translatr/components/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('UserCardLinkComponent', () => {
  let component: UserCardLinkComponent;
  let fixture: ComponentFixture<UserCardLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserCardLinkComponent, MockUserCardComponent],
      imports: [RouterTestingModule]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserCardLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
