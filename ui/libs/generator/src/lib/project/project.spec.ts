import { Project, scopes } from '@dev/translatr-model';
import { firstValueFrom, of } from 'rxjs';
import { updateRandomProject } from './project';

/**
 * Spec source of truth:
 *   openspec/changes/fix-generator-nullable-suffix-toggle/specs/load-generator-personas/spec.md
 *   Scenarios: "Updating a project that has no description",
 *              "Updating a project that already has a description"
 *
 * Regression for issue #297: updateRandomProject (Mila persona) must not throw
 * `TypeError: Cannot read properties of undefined (reading 'endsWith')` when the
 * selected project has no description.
 */
describe('updateRandomProject', () => {
  const allScopes = scopes.join(',');

  const buildServices = (project: Project) => {
    const update = jest.fn((payload: Project) => of(payload));
    const projectScoped = {
      find: jest.fn(() => of({ list: [project], total: 1, offset: 0, limit: 1 })),
      update,
    };
    const projectService = { withAuth: jest.fn(() => projectScoped) };

    const accessTokenService = {
      find: jest.fn(() =>
        of({ list: [{ key: 'user-key', scope: allScopes }], total: 1, offset: 0, limit: 20 }),
      ),
    };
    const userService = {
      find: jest.fn(() =>
        of({ list: [{ id: 'u1', username: 'repanzar' }], total: 1, offset: 0, limit: 1 }),
      ),
    };
    const noop = {} as never;

    return { update, projectService, accessTokenService, userService, noop };
  };

  it('appends the marker for a project that has no description instead of throwing', async () => {
    const project = {
      id: 'p1',
      name: 'Potter',
      ownerUsername: 'repanzar',
      description: undefined,
    } as Project;
    const { update, projectService, accessTokenService, userService, noop } = buildServices(project);

    const result = await firstValueFrom(
      updateRandomProject(
        accessTokenService as never,
        userService as never,
        projectService as never,
        noop,
        noop,
        noop,
        'default-token',
      ),
    );

    expect(update).toHaveBeenCalledTimes(1);
    expect(update.mock.calls[0][0]).toMatchObject({ id: 'p1', description: '!' });
    expect(result).toMatchObject({ description: '!' });
  });

  it('strips the trailing marker on the next pass (round-trip)', async () => {
    const project = {
      id: 'p1',
      name: 'Potter',
      ownerUsername: 'repanzar',
      description: '!',
    } as Project;
    const { update, projectService, accessTokenService, userService, noop } = buildServices(project);

    const result = await firstValueFrom(
      updateRandomProject(
        accessTokenService as never,
        userService as never,
        projectService as never,
        noop,
        noop,
        noop,
        'default-token',
      ),
    );

    expect(update.mock.calls[0][0]).toMatchObject({ description: '' });
    expect(result).toMatchObject({ description: '' });
  });

  it('turns description "Generated" into "Generated!" and back', async () => {
    const project = {
      id: 'p1',
      name: 'Potter',
      ownerUsername: 'repanzar',
      description: 'Generated',
    } as Project;
    const { update, projectService, accessTokenService, userService, noop } = buildServices(project);

    await firstValueFrom(
      updateRandomProject(
        accessTokenService as never,
        userService as never,
        projectService as never,
        noop,
        noop,
        noop,
        'default-token',
      ),
    );

    expect(update.mock.calls[0][0]).toMatchObject({ description: 'Generated!' });
  });
});
