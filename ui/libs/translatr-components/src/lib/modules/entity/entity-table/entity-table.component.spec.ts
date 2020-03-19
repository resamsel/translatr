import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityTableComponent } from './entity-table.component';
import { RouterTestingModule } from '@angular/router/testing';
import { FilterFieldTestingModule } from '@translatr/components/testing';
import { MatCheckboxModule, MatIconModule, MatPaginatorModule, MatTableModule } from '@angular/material';

describe('EntityTableComponent', () => {
  let component: EntityTableComponent;
  let fixture: ComponentFixture<EntityTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EntityTableComponent],
      imports: [
        FilterFieldTestingModule,

        RouterTestingModule,

        MatIconModule,
        MatTableModule,
        MatCheckboxModule,
        MatPaginatorModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
