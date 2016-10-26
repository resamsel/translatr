App.Core.register('ActivityModule', App.Modules.ActivityModule, {
	dataUrl: jsRoutes.controllers.Users.activityCsv(userId).url,
	messages: contributionMessages
});