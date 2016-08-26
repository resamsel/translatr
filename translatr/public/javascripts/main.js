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
	// the "href" attribute of .modal-trigger must specify the modal ID that wants to be triggered
	$('.modal-trigger').leanModal();

	$('.undo .btn-close').click(function() {
		hideUndo();
	});
	setTimeout(hideUndo, 10000);
});