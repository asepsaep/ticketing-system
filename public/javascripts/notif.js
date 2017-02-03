var interval = 1000;  // 1000 = 1 second, 3000 = 3 seconds
function doAjax() {
    $.ajax({
        type: 'GET',
        url: '/notif',
        success: function (data) {
            $("#notification-number").html(data.number);
            $("#notification-list").html(data.notification);
        },
        complete: function (data) {
            setTimeout(doAjax, interval);
        }
    });
}
setTimeout(doAjax, interval);