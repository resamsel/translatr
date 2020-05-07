const dtos = [
    'AccessToken',
    'Key',
    'LinkedAccount',
    'Locale',
    'Message',
    'Project',
    'ProjectUser',
    'User'
];
const actions = [
    'Create',
    'Update',
    'Delete'
];

const translations = {
    ...dtos.reduce((map, curr) => ({...map, [curr]: curr.toLowerCase()}), {}),
    ...actions.reduce((map, curr) => ({...map, [curr]: curr.toLowerCase() + 'd'}), {}),
    'AccessToken': 'access token',
    'LinkedAccount': 'linked account',
    'Message': 'translation',
    'ProjectUser': 'member',
    'Login': 'logged in',
    'Logout': 'logged out'
};
dtos.forEach(dto => {
    actions.forEach(action => {
        console.log(`activity.${dto.toLowerCase()}.${action.toLowerCase()} = ${translations[dto]} ${translations[action]}`);
    });
});
['Login', 'Logout'].forEach(action => {
    console.log(`activity.user.${action.toLowerCase()} = ${translations['User']} ${translations[action]}`);
});
