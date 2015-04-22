var gadget = AJS.Gadget({
    baseUrl: atlassian.util.getRendererBaseUrl(),
    useOauth: '/rest/gadget/1.0/currentUser',
    config: {
        descriptor: function (args) {
            return {
                action: null,
                theme: gadgets.window.getViewportDimensions().width < 450 ? 'gdt top-label' : 'gdt',
                fields: [
                    AJS.gadget.fields.nowConfigured(),
                    AJS.gadget.fields.projectPicker(gadget, 'projectId', args.projects)
                ]
            };
        },
        args: [
            {
                key: 'projects',
                ajaxOptions: '/rest/gadget/1.0/filtersAndProjects?showFilters=false'
            }
        ]
    },
    view: {
        enableReload: true,
        onResizeReload: false,
        onResizeAdjustHeight: true,
        template: function (args) {
            var gadget = this;
            gadgets.window.setTitle(gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.remainingBudget.title.specific', [args.data.projectName]));

            var html = '';
            if (args.data.months.length) {
                html += '<table>';
                html += '<thead>';
                html += '<th>' + args.data.paymentMonthCaption + '</th>';
                html += '<th></th>';
                html += '<th colspan="2">' + args.data.costCaption + '</th>';
                html += '</thead>';
                html += '<tbody>';
                for (var i = 0; i < args.data.months.length; i++) {
                    html += '<tr>';
                    html += '<td>';
                    html += args.data.months[i].title + '<br />';
                    html += '<a href="' + gadget.getBaseUrl() + args.data.months[i].url + '" target="_parent">' + gadget.getMsg('common.concepts.issues') + ': <strong>' + args.data.months[i].issueCount + '</strong></a>';
                    html += '</td>';
                    html += '<th>';
                    html += gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.remainingBudget.budget') + '<br />';
                    html += gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.remainingBudget.spent') + '<br />';
                    html += gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.remainingBudget.planned') + '<br />';
                    html += gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.remainingBudget.left') + '</th>';
                    html += '</th>';
                    html += '<td class="contentprojects-gauges">';
                    html += '<div class="contentprojects-gauge-brown" style="width: ' + args.data.months[i].budgetWidth + '%"></div>';
                    html += '<div class="contentprojects-gauge-yellow" style="width: ' + args.data.months[i].spentWidth + '%"></div>';
                    html += '<div class="contentprojects-gauge-blue" style="margin-left: ' + args.data.months[i].spentWidth + '%; width: ' + args.data.months[i].plannedWidth + '%"></div>';
                    html += '<div class="contentprojects-gauge-' + (args.data.months[i].budgetWidth > args.data.months[i].spentWidth + args.data.months[i].plannedWidth ? 'green' : 'red') + '" style="margin-left: ' + Math.min(args.data.months[i].budgetWidth, args.data.months[i].spentWidth + args.data.months[i].plannedWidth) + '%; width: ' + args.data.months[i].leftWidth + '%"></div>';
                    html += '</td>';
                    html += '<td>';
                    html += args.data.months[i].budget + '<br />';
                    html += args.data.months[i].spent + '<br />';
                    html += args.data.months[i].planned + '<br />';
                    html += args.data.months[i].left;
                    html += '</td>';
                    html += '</tr>';
                }
                html += '</tbody>';
                html += '</table>';
            } else {
                html += '<div class="contentprojects-footer">';
                html += gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.remainingBudget.noAvailableInformation');
                html += '</div>';
            }
            gadget.getView().html(html);
        },
        args: [
            {
                key: 'data',
                ajaxOptions: function () {
                    return {
                        url: '/rest/contentprojects/1.0/remainingBudget',
                        data: {
                            projectId: this.getPref('projectId')
                        }
                    };
                }
            }
        ]
    }
});