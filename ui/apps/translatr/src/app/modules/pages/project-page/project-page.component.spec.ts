import { Injector } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';
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

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProjectPageComponent],
        imports: [
          SidenavTestingModule,
          FeatureFlagTestingModule,

          RouterTestingModule,
          TranslocoTestingModule,

          MatTabsModule,
          MatIconModule
        ],
        providers: [
          { provide: Injector, useFactory: () => ({}) },
          {
            provide: ProjectFacade,
            useFactory: () => ({
              project$: mockObservable(),
              unload$: mockObservable()
            })
          },
          { provide: AppFacade, useFactory: () => ({}) },
          {
            provide: PROJECT_ROUTES,
            useValue: [{ children: [] }]
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
