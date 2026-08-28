import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Scope } from '@dev/translatr-model';
import { AccessTokenEditDialogComponent } from '@dev/translatr-components';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { mockObservable } from '@translatr/utils/testing';

describe('AccessTokenEditDialogComponent', () => {
  let component: AccessTokenEditDialogComponent;
  let fixture: ComponentFixture<AccessTokenEditDialogComponent>;
  let onSubmit: jest.Mock;

  beforeEach(
    waitForAsync(() => {
      onSubmit = jest.fn();
      TestBed.configureTestingModule({
        declarations: [AccessTokenEditDialogComponent],
        imports: [
          ReactiveFormsModule,
          NoopAnimationsModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatFormFieldModule,
          MatDialogModule,
          MatInputModule,
          MatSelectModule
        ],
        providers: [
          {
            provide: MatDialogRef,
            useFactory: () => ({ afterClosed: () => mockObservable(), close: jest.fn() })
          },
          {
            provide: MAT_DIALOG_DATA,
            useValue: {
              type: 'update',
              accessToken: { id: 1, name: 'ci', scope: 'read:key,write:key' },
              onSubmit,
              success$: mockObservable(),
              error$: mockObservable()
            }
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokenEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('pre-fills the form from the access token, splitting the scope', () => {
    expect(component.form.get('name').value).toEqual('ci');
    expect(component.form.get('scope').value).toEqual([Scope.KeyRead, Scope.KeyWrite]);
  });

  it('submits the token with the scope re-joined as a CSV string', () => {
    component.form.get('name').setValue('renamed');
    component.form.get('scope').setValue([Scope.ProjectRead]);

    component.onSubmit();

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ id: 1, name: 'renamed', scope: 'read:project' })
    );
  });
});
