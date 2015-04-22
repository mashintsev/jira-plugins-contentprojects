(function ($) {
    AJS.toInit(function () {
        $('#contentprojects-freelancer-add').click(function () {
            $('#contentprojects-dialog').find('.aui-dialog2-header-main').text(AJS.I18n.getText('ru.mail.jira.plugins.contentprojects.authors.freelancers.addFreelancer'));
            $('#contentprojects-dialog-ok').text(AJS.I18n.getText('common.forms.add'));
            AJS.dialog2('#contentprojects-dialog').show();
        });

        $(document).on('click', '.contentprojects-freelancer-edit', function (event) {
            event.preventDefault();
            $('#contentprojects-dialog').find('.aui-dialog2-header-main').text(AJS.I18n.getText('ru.mail.jira.plugins.contentprojects.authors.freelancers.updateFreelancer'));
            $('#contentprojects-dialog-ok').text(AJS.I18n.getText('common.forms.update'));

            var $tr = $(this).parents('.contentprojects-freelancer');
            var id = $tr.attr('id').substring('contentprojects-freelancer-'.length);

            $.ajax({
                type: 'GET',
                url: AJS.contextPath() + '/rest/contentprojects/1.0/freelancer/' + id,
                success: function (result) {
                    $('#contentprojects-dialog-id').val(id);
                    $('#contentprojects-dialog-full-name').val(result.fullName);
                    $('#contentprojects-dialog-payee-name').val(result.payeeName);
                    $('#contentprojects-dialog-contract-date').val(result.contractDate);
                    $('#contentprojects-dialog-contract-type').val(result.contractType).change();
                    $('#contentprojects-dialog-inn').val(result.inn);
                    $('#contentprojects-dialog-snils').val(result.snils);
                    AJS.dialog2('#contentprojects-dialog').show();
                },
                error: function(resp) {
                    alert(resp.responseText);
                }
            });
        });

        $(document).on('click', '.contentprojects-freelancer-delete', function (event) {
            event.preventDefault();

            var $tr = $(this).parents('.contentprojects-freelancer');
            var id = $tr.attr('id').substring('contentprojects-freelancer-'.length);

            if (confirm(AJS.I18n.getText('ru.mail.jira.plugins.contentprojects.authors.freelancers.confirmDelete')))
                $.ajax({
                    type: 'DELETE',
                    data: {
                        atl_token: atl_token()
                    },
                    url: AJS.contextPath() + '/rest/contentprojects/1.0/freelancer/' + id,
                    success: function () {
                        $tr.remove();
                    },
                    error: function (xhr) {
                        alert(xhr.responseText);
                    }
                });
        });

        $('#contentprojects-dialog-ok').click(function () {
            $('.contentprojects-dialog-error-field').removeClass('contentprojects-dialog-error-field');
            $('#contentprojects-dialog').find('.error').empty();
            $('#contentprojects-dialog-ok, #contentprojects-dialog-cancel').attr('disabled', 'disabled');

            var id = $('#contentprojects-dialog-id').val();
            var fullName = $('#contentprojects-dialog-full-name').val();
            var payeeName = $('#contentprojects-dialog-payee-name').val();
            var contractDate = $('#contentprojects-dialog-contract-date').val();
            var contractType = $('#contentprojects-dialog-contract-type').val();
            var contractTypeText = $('#contentprojects-dialog-contract-type').find('option:selected').text();
            var inn = $('#contentprojects-dialog-inn').val();
            var snils = $('#contentprojects-dialog-snils').val();

            if (id) {
                $.ajax({
                    type: 'PUT',
                    url: AJS.contextPath() + '/rest/contentprojects/1.0/freelancer/' + id,
                    data: {
                        atl_token: atl_token(),
                        fullName: fullName,
                        payeeName: payeeName,
                        contractDate: contractDate,
                        type: contractType,
                        inn: inn,
                        snils: snils
                    },
                    success: function () {
                        AJS.dialog2('#contentprojects-dialog').hide();
                        $('#contentprojects-dialog-ok, #contentprojects-dialog-cancel').removeAttr('disabled');

                        var $tr = $('#contentprojects-freelancer-' + id);
                        $tr.find('.contentprojects-freelancer-full-name').text(fullName);
                        $tr.find('.contentprojects-freelancer-payee-name').text(payeeName);
                        $tr.find('.contentprojects-freelancer-contract-date').text(contractDate);
                        $tr.find('.contentprojects-freelancer-contract-type').text(contractTypeText);
                        $tr.find('.contentprojects-freelancer-inn').text(inn);
                        $tr.find('.contentprojects-freelancer-snils').text(snils);
                    },
                    error: handleCreateAndUpdateError
                });
            } else {
                $.ajax({
                    type: 'POST',
                    url: AJS.contextPath() + '/rest/contentprojects/1.0/freelancer',
                    data: {
                        atl_token: atl_token(),
                        fullName: fullName,
                        payeeName: payeeName,
                        contractDate: contractDate,
                        type: contractType,
                        inn: inn,
                        snils: snils
                    },
                    success: function (result) {
                        AJS.dialog2('#contentprojects-dialog').hide();
                        $('#contentprojects-dialog-ok, #contentprojects-dialog-cancel').removeAttr('disabled');

                        var html = '';
                        html += '<tr id="contentprojects-freelancer-' + result + '" class="contentprojects-freelancer">';
                        html += '<td class="contentprojects-freelancer-full-name">' + fullName + '</td>';
                        html += '<td class="contentprojects-freelancer-payee-name">' + payeeName + '</td>';
                        html += '<td class="contentprojects-freelancer-contract-date">' + contractDate + '</td>';
                        html += '<td class="contentprojects-freelancer-contract-type">' + contractTypeText + '</td>';
                        html += '<td class="contentprojects-freelancer-inn">' + inn + '</td>';
                        html += '<td class="contentprojects-freelancer-snils">' + snils + '</td>';
                        html += '<td>';
                        html += '<ul class="operations-list">';
                        html += '<li><a href="#" class="contentprojects-freelancer-edit">' + AJS.I18n.getText('common.forms.edit') + '</a></li>&nbsp;';
                        html += '<li><a href="#" class="contentprojects-freelancer-delete">' + AJS.I18n.getText('common.words.delete') + '</a></li>';
                        html += '</ul>';
                        html += '</td>';
                        html += '</tr>';
                        $('#contentprojects-freelancers').prepend(html);
                    },
                    error: handleCreateAndUpdateError
                });
            }
        });

        function handleCreateAndUpdateError(req) {
            var field = req.getResponseHeader('X-Atlassian-Rest-Exception-Field');
            if (field) {
                $('#contentprojects-dialog-' + field).addClass('contentprojects-dialog-error-field').focus();
                $('#contentprojects-dialog-' + field + '-error').text(req.responseText);
            }
            $('#contentprojects-dialog-ok, #contentprojects-dialog-cancel').removeAttr('disabled');
        }

        $('#contentprojects-dialog-cancel').click(function () {
            AJS.dialog2('#contentprojects-dialog').hide()
        });

        AJS.dialog2('#contentprojects-dialog').on('hide', function ( ) {
            $('.contentprojects-dialog-error-field').removeClass('contentprojects-dialog-error-field');
            $(this).find('.error').empty();

            $(this).find('input').val('');
            var $contractType = $('#contentprojects-dialog-contract-type');
            $contractType.val($contractType.find('option:first').val()).change();
        });

        AJS.$('#contentprojects-dialog-contract-date').datePicker({
            dateFormat: 'dd/mm/yy',
            overrideBrowserDefault: true
        });

        $('#contentprojects-dialog-contract-type').change(function () {
            var value = $(this).val();
            $('#contentprojects-dialog-inn-req').toggleClass('hidden', value != 'SOLE_PROPRIETORSHIP');
            $('#contentprojects-dialog-snils-req').toggleClass('hidden', value != 'CIVIL_LAW_CONTRACT');
        });

        $('#contentprojects-dialog').find('form').submit(function (e) {
            e.preventDefault();
            $('#contentprojects-dialog-ok').click();
        });
    })
})(AJS.$);