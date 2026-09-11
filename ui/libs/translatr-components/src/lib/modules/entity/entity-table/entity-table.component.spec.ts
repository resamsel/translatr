import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { FilterFieldTestingModule } from '@translatr/components/testing';
import { EntityTableComponent } from './entity-table.component';

describe('EntityTableComponent', () => {
  let component: EntityTableComponent;
  let fixture: ComponentFixture<EntityTableComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [EntityTableComponent],
        imports: [
          FilterFieldTestingModule,

          RouterTestingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatIconModule,
          MatTableModule,
          MatCheckboxModule,
          MatPaginatorModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders its search field with an outline appearance, matching the rest of the admin UI', () => {
    const filterField = fixture.debugElement.query(By.css('dev-filter-field'));
    expect(filterField.componentInstance.appearance).toBe('outline');
  });
});
