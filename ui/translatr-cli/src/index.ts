const TSModuleAlias = require("@momothepug/tsmodule-alias");
// Path from package.json to your tsconfig.json file
const tsconfigToReadFromRoot = "./";
// Makes it work with play method
const aliasRegister = TSModuleAlias.play(tsconfigToReadFromRoot);
// Alias map loaded to nodejs from typescript paths (optional)
console.log('aliasMap', aliasRegister.nodeRegister.aliasMap);
// Displays root module and typescript project path (optional)
console.log('env', aliasRegister.currentEnvironmentData);

export {run} from '@oclif/command'
