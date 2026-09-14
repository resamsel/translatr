// Type-only re-export of the OpenAPI-generated response DTOs shared with the
// web UI, so the CLI's response shapes can't silently drift from the server
// contract. These are plain interfaces with no framework imports (see
// ui/libs/translatr-sdk/src/lib/generated/model/) - only type information is
// pulled in, no runtime code from translatr-sdk is bundled into the CLI.
export type { ProjectDto } from "../../../libs/translatr-sdk/src/lib/generated/model/projectDto";
export type { LocaleDto } from "../../../libs/translatr-sdk/src/lib/generated/model/localeDto";
export type { KeyDto } from "../../../libs/translatr-sdk/src/lib/generated/model/keyDto";
export type { UserDto } from "../../../libs/translatr-sdk/src/lib/generated/model/userDto";
