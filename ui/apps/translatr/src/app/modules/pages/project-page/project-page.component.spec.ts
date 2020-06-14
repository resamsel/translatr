import { Injector } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterTestingModule } from '@angular/router/testing';
import { FeatureFlagTestingModule } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavTestingModule } from '../../nav/sidenav/testing';
import { ProjectFacade } from '../../shared/project-state';

import { ProjectPageComponent } from './project-page.component';
import { PROJECT_ROUTES } from './project-page.token';

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
          useValue: [{ children: [] }]
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
