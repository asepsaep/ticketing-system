jQuery(document).ready(function($){

    $('.tdtime').each(function(i, e) {
        var time = moment($(e).html());
        $(e).text(moment(time).fromNow());
    });

});