import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectPageComponent } from './project-page.component';
import { Injector } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { ProjectFacade } from './+state/project.facade';
import { AppFacade } from '../../../+state/app.facade';
import { PROJECT_ROUTES } from './project-page.token';
import { SidenavTestingModule } from '../../nav/sidenav/testing';
import { FeatureFlagTestingModule } from '@translatr/components/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { mockObservable } from '@translatr/utils/testing';

describe('ProjectPageComponent', () => {
  let component: ProjectPageComponent;
  let fixture: ComponentFixture<ProjectPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectPageComponent],
      imports: [
        SidenavTestingModule,
        FeatureFlagTestingModule,

        RouterTestingModule,

        MatTabsModule,
        MatIconModule
      ],
      providers: [
        { provide: Injector, useFactory: () => ({}) },
        {
          provide: ProjectFacade,
          useFactory: () => ({
            project$: mockObservable()
          })
        },
        { provide: AppFacade, useFactory: () => ({}) },
        {
          provide: PROJECT_ROUTES,
          useValue: ([
            { children: [] }
          ])
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
