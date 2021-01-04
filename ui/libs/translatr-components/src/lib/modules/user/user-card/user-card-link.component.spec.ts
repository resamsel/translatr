import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { UserCardLinkComponent } from '@dev/translatr-components';
import { MockUserCardComponent } from '@translatr/components/testing';

describe('UserCardLinkComponent', () => {
  let component: UserCardLinkComponent;
  let fixture: ComponentFixture<UserCardLinkComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UserCardLinkComponent, MockUserCardComponent],
        imports: [RouterTestingModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserCardLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
