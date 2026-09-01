import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Feature } from '@dev/translatr-model';
import { GlobalFeatureFlagService } from './global-feature-flag.service';

describe('GlobalFeatureFlagService', () => {
  let service: GlobalFeatureFlagService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(GlobalFeatureFlagService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list() GETs the global collection', () => {
    service.list().subscribe();
    const req = http.expectOne('/api/featureflags/global');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('set() POSTs feature + enabled', () => {
    service.set(Feature.HeaderGraphic, true).subscribe();
    const req = http.expectOne('/api/featureflag/global');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ feature: Feature.HeaderGraphic, enabled: true });
    req.flush({ feature: Feature.HeaderGraphic, enabled: true });
  });

  it('delete() DELETEs by id', () => {
    service.delete('gff-1').subscribe();
    const req = http.expectOne('/api/featureflag/global/gff-1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
