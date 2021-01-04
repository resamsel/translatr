import { TestBed, waitForAsync } from '@angular/core/testing';
import { TranslatrModelModule } from './translatr-model.module';

describe('TranslatrModelModule', () => {
  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [TranslatrModelModule]
      }).compileComponents();
    })
  );

  it('should create', () => {
    expect(TranslatrModelModule).toBeDefined();
  });
});
