(function ($) {
    AJS.toInit(function () {
        if ($('#statistics-panel').length) {
            function init() {
                $('.contentprojects-arrow').remove();
                $('#statistics-panel').find('dl').each(function () {
                    var selector = $(this).data('selector');
                    var greater = $(this).data('greater');
                    var better = $(this).data('better');
                    if (selector) {
                        var arrow = $('<span class="contentprojects-arrow aui-icon aui-icon-small" />');
                        arrow.addClass(greater ? 'aui-iconfont-up' : 'aui-iconfont-down');
                        arrow.attr('title', greater ? AJS.I18n.getText('ru.mail.jira.plugins.contentprojects.issue.statisticsPanel.greater') : AJS.I18n.getText('ru.mail.jira.plugins.contentprojects.issue.statisticsPanel.less'));
                        arrow.css('color', better ? '#14892c' : '#d04437');
                        $(selector).append(' ', arrow);
                    }
                });
            }

            init();
            JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $context, reason) {
                if (reason == JIRA.CONTENT_ADDED_REASON.panelRefreshed && $context.is('#statistics-panel, #details-module'))
                    init();
            });
        }
    });
})(AJS.$);
