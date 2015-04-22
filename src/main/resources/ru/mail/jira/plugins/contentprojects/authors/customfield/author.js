(function ($) {
    AJS.toInit(function () {
        function getAvatarHtml(object) {
            var avatarUrl = $(object.element).data('avatar-url');
            if (avatarUrl)
                return '<span class="aui-avatar aui-avatar-xsmall"><span class="aui-avatar-inner"><img src="' + avatarUrl + '"></span></span>';
            else
                return '';
        }

        function init(context) {
            $(context).find('.contentprojects-author').auiSelect2({
                allowClear: true,
                formatSelection: function (object, container, escapeMarkup) {
                    return getAvatarHtml(object) + escapeMarkup($(object.element).data('caption'));
                },
                formatResult: function (object, container, query, escapeMarkup) {
                    var markup = [];
                    Select2.util.markMatch(object.text, query.term, markup, escapeMarkup);
                    return getAvatarHtml(object) + ' ' + markup.join('');
                }
            });
        }

        init(document);
        JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
            if (reason == JIRA.CONTENT_ADDED_REASON.inlineEditStarted || reason == JIRA.CONTENT_ADDED_REASON.dialogReady)
                init(context);
        });
    });
})(AJS.$);