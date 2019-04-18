import { async, TestBed } from '@angular/core/testing';
import { TranslatrModelModule } from './translatr-model.module';

describe('TranslatrModelModule', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [TranslatrModelModule]
    }).compileComponents();
  }));

  it('should create', () => {
    expect(TranslatrModelModule).toBeDefined();
  });
});
