/**
 * Created by Andrey on 1/16/2017.
 */
$(document).ready(function () {
    // Set up today's date in header
    $('#date').text(moment().format('MMM Do YY'));

    var table = $('#ordsTable').DataTable({
        "lengthMenu": [[5, 10, 15, 30], [5, 10, 15, 30]],
        "ajax": {
            "url": "/chef/get_orders_today",
            "type": "POST",
            "dataType": "json"
        },
        order: [[ 1, "desc" ]],
        columnDefs: [
            {"width": "5%", "targets": 0},
            {"width": "30%", "targets": 1},
            {"width": "40%", "targets": 2},
            {"width": "10%", "targets": 5}
        ],
        columns: [
            {"data": "id", "name": "id", "title": "#", "visible": true},
            {"data": "waiter", "name": "waiter", "title": "waiter"},
            {"data": "openedTimeStamp", "name": "openedTimeStamp", "title": "opened time"},
            {"data": "dishes", "name": "dishes quantity", "title": "dishes quantity"},
                {"data": null, "name": "isFulfilled", "title": "isFulfilled", "render": function(data){
                if (data.isFulfilled) {
                    return '<p hidden="true">true</p><input type="checkbox" disabled="true" checked/>';
                } else {
                    return '<p hidden="true">false</p><input type="checkbox" disabled="true"/>';
                }
            }},
            {"data": null, "name": "isCancelled", "title": "isCancelled", "render": function(data){
                if (data.isCancelled) {
                    return '<p hidden="true">true</p><input type="checkbox" disabled="true" checked/>';
                } else {
                    return '<p hidden="true">false</p><input type="checkbox" disabled="true"/>';
                }
            }},
            {"data": null, "sortable": false, "render": function () {
                return '<button class="btn btn-default">Details</button>';
            }
            }
        ]
    });

    // Onclick on a table row to show an Order's dishes
    $('#ordsTable tbody').on('click', 'button', function () {
        var data = table.row($(this).parents('tr')).data();
        $('#orderId').text(data.id);
        $('#waiter').text(data.waiter);
        var table2 = $('#dishesTable').DataTable()
            .ajax.url(
                "/chef/get_orders_prepared_dishes?orderId="
                + encodeURIComponent(data.id)
            )
            .load()
    });

    $('#dishesTable').DataTable({
        bLengthChange: false,
        columnDefs: [
            {"width": "5%", "targets": 0},
            {"width": "50%", "targets": 1},
            {"width": "10%", "targets": 4}
        ],
        columns: [
            {"data": "id", "name": "id", "title": "#", "visible": true},
            {"data": "dish", "name": "dish", "title": "dish"},
            {"data": "quantity", "name": "quantity", "title": "quantity"},
            {"data": null, "name": "isPrepared", "title": "isPrepared", "render": function(data){
                if (data.isPrepared) {
                    return '<p hidden="true">true</p><input type="checkbox" disabled="true" checked/>';
                } else {
                    return '<p hidden="true">false</p><input type="checkbox" disabled="true"/>';
                }
            }},
            {"data": null, "name": "isReturned", "title": "isReturned", "render": function(data){
                if (data.isReturned) {
                    return '<p hidden="true">true</p><input type="checkbox" disabled="true" checked/>';
                } else {
                    return '<p hidden="true">false</p><input type="checkbox" disabled="true"/>';
                }
            }},
            {
                "data": null, "sortable": false, "render": function (data) {
                if (data.isPrepared || data.isReturned) {
                    return '<input type="button" class="btn btn-default" disabled="true" value="Confirm"/>';
                } else {
                    var params = 'dish=' + data.dish+ '&dishId=' + data.id+'&quantity='+data.quantity +'&orderId='+$('#orderId').text();
                    return '<a href="javascript:checkOrder(\'' + params + '\')">' +
                                '<input type="button" class="btn btn-default" value="Confirm"/>' +
                            '</a>';
                }
            }
            }
        ]
    });
});

function checkOrder(params) {
    var url = "/chef/check_order?"+params;
    console.log("url: "+url);
    $.ajax({
        type: 'GET',
        url: url,
        contentType: 'application/json;',
        dataType: 'json',
        success: prepare,
        error: function(xhr, textStatus, errorThrown) {
            console.log('error at checkOrder status: '+textStatus+' error name: '+errorThrown);
        }
    });
}

function prepare(data) {
    // Reload Dishes table
    var params = 'dish=' + data.dish+ '&dishId=' + data.dishId + '&quantity=' + data.quantity + '&orderId=' + data.orderId;
    var url = "/chef/confirm_dishes_prepared?"+params;
    if (data.isCancelled) {
        if (confirm('The Order was cancelled! Do you want to return ingredients?')) {
            url = '/chef/confirm_dishes_cancelled?'+params;
        }
    }
    $.ajax({
        type: 'GET',
        url: url,
        contentType: 'application/json;',
        dataType: 'json',
        success: reloadDishesTable,
        error: function(xhr, textStatus, errorThrown) {
            console.log('error at prepare, status: '+textStatus+' error name: '+errorThrown);
        }
    });
}

function reloadDishesTable(data) {
    $('#dishesTable').DataTable()
        .ajax.url(
        "/chef/get_orders_prepared_dishes?orderId="
        + encodeURIComponent(data.orderId)
    ).load();
    sendMessage(data);
}

function reloadOrdersTable(data) {
    $('#ordsTable').DataTable()
        .ajax.url(
        "/chef/get_orders_today"
    ).load()
}

function sendMessage(data) {
    var stompClient = null;
    var socket = new SockJS('/messaging/chef');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        var message = new Object();
        message.time = moment(new Date()).format('HH:mm');
        message.order = data.orderId;
        message.dish = data.dish;
        message.quantity = data.quantity;
        message.action = "prepared";
        message.chef = $('#username').val();
        var url = "/user/"+$('#waiter').text()+"/queue/waiter";
        stompClient.send(url, {}, JSON.stringify(message));
        stompClient.disconnect();
    });
}