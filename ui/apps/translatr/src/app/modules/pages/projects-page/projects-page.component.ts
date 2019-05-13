import {Component, OnDestroy, OnInit} from '@angular/core';
import {ProjectsFacade} from './+state/projects.facade';
import {AppFacade} from '../../../+state/app.facade';

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  projects$ = this.facade.allProjects$;

  constructor(private readonly facade: ProjectsFacade, private readonly appFacade: AppFacade) {
  }

  ngOnInit() {
    this.onLoadProjects(20);
  }

  ngOnDestroy(): void {
    this.facade.unloadProjects();
  }

  onLoadProjects(limit: number) {
    this.facade.loadProjects({order: 'whenUpdated desc', limit: `${limit}`});
  }
}
