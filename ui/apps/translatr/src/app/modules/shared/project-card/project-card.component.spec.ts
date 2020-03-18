import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectCardComponent } from './project-card.component';
import { MatCardModule, MatTooltipModule } from '@angular/material';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';

describe('ProjectCardComponent', () => {
  let component: ProjectCardComponent;
  let fixture: ComponentFixture<ProjectCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectCardComponent],
      imports: [
        GravatarModule,
        MomentModule,

        MatCardModule,
        MatTooltipModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
