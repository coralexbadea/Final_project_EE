// Set up tables
$(document).ready(function () {
    $.ajax({
        type: "POST",
        data: null,
        url: '/waiter/get_tables',
        dataType: 'json',
        success: function(json) {
            var $el = $("#table");
            $el.empty();
            $.each(json, function(value, key) {
                $el.append($("<option></option>")
                    .attr("value", value).text(key));
            });
        }
    });

    // Make selected the Order's table
    $.ajax({
        type: "POST",
        data: {"orderId": $("#id").val()},
        url: '/waiter/get_orders_table',
        dataType: 'json',
        success: function(json) {
            //var $el = $("#table");
            $("#table").val(json);
        }
    });
});

// Set up Order's dishes
$(document).ready(function () {
    console.log("id= "+$("#id").val());
    var table = $('#odTable').DataTable({
        bLengthChange: false,
        "ajax" : {
            "data": {"orderId": $("#id").val()},
            "url": "/waiter/get_orders_dishes",
            "type": "POST",
            "dataType": "json"
        },
        columns: [
            { "data": "id", "visible": false, "searchable": false},
            { "data": "name"},
            { "data": "quantity", "render": function(data) {
                return '<input type="text" value = "' + data+ '" size = "2" name="input"/>'
            }
            },
            { "data": "price"},
            { "data": null, "render": function() {
                return '<button type="button" class="btn btn-default" id="delRow" name ="delRow">Del</button>'
            }
            }
        ]
    });
    // Remove a selected dish from order
    $('#odTable tbody').on('click', 'button', function () {
        table.row($(this).parents('tr')).remove().draw();
    });
});

// Set up a Dish table in modal window
$(document).ready(function () {
    var table = $('#dTable').DataTable({
        "ajax" : {
            "url": "/waiter/get_dishes",
            "type": "POST",
            "dataType": "json"
        },
        serverSide: true,
        columnDefs: [
            { "width": "5%", "targets": 0 },
            { "width": "30%", "targets": 1 },
            { "width": "30%", "targets": 2 },
            { "width": "10%", "targets": 3 },
            { "width": "10%", "targets": 4 },
            { "width": "15%", "targets": 5 }
        ],
        columns: [
            { "data": "id", "name": "id",  "title": "id", "visible": false},
            { "data": "name", "name": "name", "title": "Dish"},
            { "data": "category", "name": "category", "title": "Category"},
            { "data": "weight", "name": "weight", "title": "Weight, g"},
            { "data": "price", "name": "price", "title": "Price, $"},

            { "data": null,"defaultContent": "<button>Add</button>"}
        ]
    });

    //Adds a selected dish to the order
    $('#dTable tbody').on('click', 'button', function () {
        var data = table.row($(this).parents('tr')).data();
        var t = $('#odTable').DataTable();
        t.row.add({
            "id": data.id,
            "name": data.name,
            "quantity": 1,
            "price": data.price
        }).draw();
    });
});

// Gets the Order object from server
$(document).ready(function () {
    var currentOrder;
    $.ajax({
        url: "/waiter/get_order?orderId="+$("#id").val(),
        type: "POST",
        dataType: "json",
        success: function (order) {
            currentOrder = order;
        }
    });

    // Only table and dishes can be changed!!
    $('#editOrderForm').submit(function (event) {
        var updatedOrder = new Object();
        updatedOrder.id = currentOrder.id;
        updatedOrder.table = $('#table').val();
        updatedOrder.dishes = getDishes();
        $.ajax({
            url: "/waiter/edit_order",
            dataType: 'json',
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(updatedOrder),
            success: function (data) {
                console.log("Success");
                sendMessage(data);
                // TODO close tab and reload ordersTable
                // location.href='/waiter/orders/today';
            },
            error: function (e) {
                console.log("ERROR: ", e);
                display(e);
            },
            done: function (e) {
                console.log("DONE");
            }
        });
        event.preventDefault();
    });

    // Prepares a dish array object as a part of data to be sent to the server
    function getDishes() {
        var odTable = $('#odTable').DataTable();
        var dishes = [];
        //<![CDATA[
        var dishIdArray = odTable
            .columns(0)
            .data()
            .eq(0)
            .toArray();
        var quantityString = odTable
            .columns(2)
            .data()
            .eq(0)
            .$('input')
            .serialize();
        var quantityArray = quantityString.split("&");
        var len = dishIdArray.length;
        for (var i = 0; i < len; i++) {
            dishes.push({
                dishId: dishIdArray[i],
                quantity: parseInt(quantityArray[i]
                    .slice(6))
            });
        }
        //]]>
        return dishes;
    }

    // Displays the server's feedback on the page
    function display(data) {
        var json = "<h4>Error</h4><pre>"
            + data.responseText+ "</pre>";
        $('#feedback').html(json);
    }
});

// Makes the modal window draggable
$(document).ready(function () {
    $("#myModal").draggable({
        handle: ".modal-header"
    })
});

function sendMessage(data) {
    var stompClient = null;
    var socket = new SockJS('/messaging/waiter');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.send("/app/messaging/waiter", {}, JSON.stringify(
            {'time': 'set on server', "action": "edited", 'order': ' Order# '+data.id,  "waiter": ' by '+data.waiterName}
            )
        );
        stompClient.disconnect();
        location.href = '/waiter/orders/today';
    });
}