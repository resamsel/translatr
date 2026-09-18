import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { UserCardComponent, UserCardLinkComponent } from '@dev/translatr-components';
import { UserCardTestingModule } from '@translatr/components/testing';

describe('UserCardLinkComponent', () => {
  let component: UserCardLinkComponent;
  let fixture: ComponentFixture<UserCardLinkComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UserCardLinkComponent, {
        remove: { imports: [UserCardComponent] },
        add: { imports: [UserCardTestingModule] }
      }).configureTestingModule({
        imports: [UserCardLinkComponent, RouterTestingModule]
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
