var interval = 1000;
function getNotif() {
    $.ajax({
        type: 'GET',
        url: '/notif',
        success: function(data) {
            $("#notification-number").html(data.number);
            $("#notification-list").html(data.notification);
        },
        complete: function(data) {
            setTimeout(getNotif, interval);
        }
    });
}
setTimeout(getNotif, interval);
