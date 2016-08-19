function hideUndo() {
	$('.undo').animate(
		{ opacity: 0 },
		400,
		function() {
			$(this).hide();
		}
	);
}
$(document).ready(function() {
	$('.undo .btn-close').click(function() {
		hideUndo();
	});
	setTimeout(hideUndo, 10000);
});