import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Project} from "../../../../shared/project";
import {ActivatedRoute} from "@angular/router";
import {Locale} from "../../../../shared/locale";
import {Key} from "../../../../shared/key";
import { Observable } from "rxjs";
import { PagedList } from "../../../../shared/paged-list";
import { Aggregate } from "../../../../shared/aggregate";
import { UserService } from "../../../../services/user.service";
import { User } from "../../../../shared/user";
import { ProjectService } from "../../../../services/project.service";
import { ActivityService } from "../../../../services/activity.service";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-info',
  templateUrl: './project-info.component.html',
  styleUrls: ['./project-info.component.scss']
})
export class ProjectInfoComponent implements OnInit {

  project: Project;
  activity$: Observable<PagedList<Aggregate> | undefined>;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly projectService: ProjectService,
    private readonly activityService: ActivityService) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { project: Project }) => {
        this.project = data.project;
        this.activity$ = this.activityService.aggregated({projectId: data.project.id});
      });
  }

  latestLocales(): Array<Locale> {
    if (!this.project || !this.project.locales) {
      return [];
    }

    const locales = this.project.locales;

    return locales
      .sort((a: Locale, b: Locale) => b.whenUpdated.getTime() - a.whenUpdated.getTime())
      .slice(0, 3);
  }

  latestKeys(): Array<Key> {
    if (!this.project || !this.project.keys) {
      return [];
    }

    const keys = this.project.keys;

    return keys
      .sort((a: Key, b: Key) => b.whenUpdated.getTime() - a.whenUpdated.getTime())
      .slice(0, 3);
  }
}
