App.Core.register('ActivityModule', App.Modules.ActivityModule, {
	dataUrl: jsRoutes.controllers.Users.activityCsv(userId).url,
	cellUrl: jsRoutes.controllers.Profiles.activity().url,
	messages: contributionMessages
});