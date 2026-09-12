import type { Command } from "commander";
import { Api, isUuid } from "../api.js";
import { readConfigMerge } from "../config.js";
import { printTable } from "../print.js";
import type { GlobalOptions } from "../config.js";

export function registerProject(program: Command, getGlobal: () => GlobalOptions): void {
  const project = program.command("project").description("project commands");

  project
    .command("ls")
    .description("list projects")
    .argument("[search]", "the search string")
    .action(async (search?: string) => {
      const config = readConfigMerge(getGlobal());
      const projects = await new Api(config).projects(search);
      printTable(projects.map((p) => [p.id, p.name, p.ownerName]));
    });

  project
    .command("create")
    .description("create project")
    .argument("<project_name>", "the project name")
    .action(async (projectName: string) => {
      const config = readConfigMerge(getGlobal());
      const p = await new Api(config).projectCreate(projectName);
      console.log(`Project ${p.name} has been created (ID: ${p.id})`);
    });

  project
    .command("rm")
    .description("remove projects")
    .argument("<project_ids...>", "the project IDs (or names)")
    .action(async (projectIds: string[]) => {
      const config = readConfigMerge(getGlobal());
      const api = new Api(config);
      for (let projectId of projectIds) {
        if (!isUuid(projectId)) {
          const matches = (await api.projects(projectId)).filter((p) => p.name === projectId);
          if (matches.length === 0) {
            throw new Error(`Project with ID '${projectId}' not found`);
          }
          projectId = matches[0].id;
        }
        const p = await api.projectDelete(projectId);
        console.log(`Project ${p.name.replace(`${projectId}-`, "")} has been deleted`);
      }
    });
}
