var gadget = AJS.Gadget({
    baseUrl: atlassian.util.getRendererBaseUrl(),
    useOauth: '/rest/gadget/1.0/currentUser',
    config: {
        descriptor: function (args) {
            var searchParam;
            if (/^jql-/.test(this.getPref('projectOrFilterId')))
                searchParam = {
                    userpref: 'projectOrFilterId',
                    type: 'hidden',
                    value: gadgets.util.unescapeString(this.getPref('projectOrFilterId'))
                };
            else
                searchParam = AJS.gadget.fields.projectOrFilterPicker(this, 'projectOrFilterId');

            return {
                action: '/rest/contentprojects/1.0/numberFieldStatistics/validate',
                theme: gadgets.window.getViewportDimensions().width < 450 ? 'gdt top-label' : 'gdt',
                fields: [
                    AJS.gadget.fields.nowConfigured(),
                    searchParam,
                    columnGadgetFieldType(gadget, 'fieldIds', args.availableColumns.availableColumns),
                    {
                        userpref: 'groupFieldId',
                        label: gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.numberFieldStatistics.groupField'),
                        type: 'select',
                        selected: this.getPref('groupFieldId'),
                        options: [
                            {
                                label: gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.numberFieldStatistics.doNotGroup'),
                                value: ''
                            }
                        ].concat(args.statTypes.stats)
                    }
                ]
            };
        },
        args: [
            {
                key: 'availableColumns',
                ajaxOptions: '/rest/gadget/1.0/availableColumns'
            },
            {
                key: 'statTypes',
                ajaxOptions: '/rest/gadget/1.0/statTypes'
            }
        ]
    },
    view: {
        enableReload: true,
        onResizeReload: false,
        onResizeAdjustHeight: true,
        template: function (args) {
            var gadget = this;

            gadget.projectOrFilterName = args.data.projectOrFilterName; // This is used to pass the name of filter or project to the picker
            gadgets.window.setTitle(gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.numberFieldStatistics.title.specific', [args.data.projectOrFilterName]));

            var totalIssuesLinkStart = '<a href="' + gadget.getBaseUrl() + args.data.url + '" target="_parent" title="' + gadgets.util.escapeString(args.data.projectOrFilterName) + '">';
            var totalIssuesLinkEnd = '</a>';

            function getGroupFieldName() {
                for (var i = 0; i < args.statTypes.stats.length; i++)
                    if (args.statTypes.stats[i].value == args.data.groupFieldId)
                        return args.statTypes.stats[i].label;
                return '';
            }

            function getTotalIssueCount() {
                var result = 0;
                for (var i = 0; i < args.data.groups.length; i++)
                    result += args.data.groups[i].issueCount;
                return result;
            }

            var html = '';
            if (args.data.groups.length) {
                html += '<div class="contentprojects-wrapper">';
                html += '<table>';
                html += '<thead>';
                if (args.data.groupFieldId) {
                    var className = 'sortable';
                    if (gadget.sortFieldIndex == -1)
                        className += ' active' + (gadget.sortDesc ? ' descending' : ' ascending');
                    html += '<th class="' + className + '" data-field-index="-1"><span>' + getGroupFieldName() + '</span></th>';
                }
                html += '<th></th>';
                for (var i = 0; i < args.data.fieldNames.length; i++) {
                    className = '';
                    if (args.data.groupFieldId) {
                        className += 'sortable';
                        if (gadget.sortFieldIndex == i)
                            className += ' active' + (gadget.sortDesc ? ' descending' : ' ascending');
                    }
                    html += '<th class="' + className + '" data-field-index="' + i + '"><span>' + args.data.fieldNames[i] + '</span></th>';
                }
                html += '</thead>';
                html += '<tbody>';
                for (i = 0; i < args.data.groups.length; i++) {
                    html += '<tr>';
                    if (args.data.groupFieldId) {
                        html += '<td>';
                        html += args.data.groups[i].title + '<br />';
                        html += '<a href="' + gadget.getBaseUrl() + args.data.groups[i].url + '" target="_parent">' + gadget.getMsg('common.concepts.issues') + ': <strong>' + args.data.groups[i].issueCount + '</strong></a>';
                        html += '</td>';
                    }
                    html += '<th>' + gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.numberFieldStatistics.median') + '<br />' + gadget.getMsg('ru.mail.jira.plugins.contentprojects.gadgets.numberFieldStatistics.mean') + '</th>';
                    for (var j = 0; j < args.data.groups[i].fieldStatisticValues.length; j++)
                        html += '<td>' + (args.data.groups[i].fieldStatisticValues[j] ? args.data.groups[i].fieldStatisticValues[j].median + '<br />' + args.data.groups[i].fieldStatisticValues[j].mean : '') + '</td>';
                    html += '</tr>';
                }
                html += '</tbody>';
                html += '</table>';
                html += '<div class="contentprojects-footer">';
                html += totalIssuesLinkStart + gadget.getMsg('common.concepts.total.issues', ['<strong>' + getTotalIssueCount() + '</strong>']) + totalIssuesLinkEnd;
                html += '</div>';
                html += '</div>';
            } else {
                html += '<div class="contentprojects-footer">';
                html += gadget.getMsg('gadget.issuetable.common.empty', [totalIssuesLinkStart, totalIssuesLinkEnd]);
                html += '</div>';
            }
            gadget.getView().html(html);

            AJS.$('th.sortable').click(function () {
                var fieldIndex = AJS.$(this).data('field-index');
                gadget.sortDesc = (fieldIndex == gadget.sortFieldIndex) ? !gadget.sortDesc : false;
                gadget.sortFieldIndex = fieldIndex;
                gadget.getView().empty();
                gadget.showView(true);
            });
        },
        args: [
            {
                key: 'data',
                ajaxOptions: function () {
                    return {
                        url: '/rest/contentprojects/1.0/numberFieldStatistics',
                        data: {
                            projectOrFilterId: gadgets.util.unescapeString(this.getPref('projectOrFilterId')),
                            fieldIds: this.getPrefArray('fieldIds'),
                            groupFieldId: this.getPref('groupFieldId'),
                            sortFieldIndex: this.sortFieldIndex,
                            sortDesc: this.sortDesc
                        }
                    };
                }
            },
            {
                key: 'statTypes',
                ajaxOptions: '/rest/gadget/1.0/statTypes'
            }
        ]
    }
});