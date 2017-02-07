var interval = 1000;
function getBuildLog() {
    var id = $('#buildId')[0].value
    $.ajax({
        type: 'GET',
        url: '/buildlog/' + id,
        success: function(data) {
            $('#log').html(data.log)
        },
        complete: function(data) {
            setTimeout(getBuildLog, interval);
        }
    });
}
setTimeout(getBuildLog, interval);