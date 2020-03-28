import { LayoutModule } from '@angular/cdk/layout';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { MainPageComponent } from './main-page.component';
import {
  ActivityGraphTestingModule,
  FeatureFlagTestingModule,
  FooterTestingModule,
  NavbarTestingModule
} from '@translatr/components/testing';
import { MatDividerModule, MatTooltipModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';
import { ActivityService } from '@dev/translatr-sdk';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('MainPageComponent', () => {
  let component: MainPageComponent;
  let fixture: ComponentFixture<MainPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MainPageComponent],
      imports: [
        NavbarTestingModule,
        FooterTestingModule,
        ActivityGraphTestingModule,
        FeatureFlagTestingModule,

        NoopAnimationsModule,
        RouterTestingModule,
        LayoutModule,
        TranslocoTestingModule,

        MatButtonModule,
        MatCardModule,
        MatGridListModule,
        MatIconModule,
        MatMenuModule,
        MatDividerModule,
        MatTooltipModule
      ],
      providers: [
        {
          provide: AppFacade,
          useFactory: () => ({
            me$: mockObservable()
          })
        },
        {
          provide: ActivityService,
          useFactory: () => ({
            aggregated: () => mockObservable()
          })
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MainPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
