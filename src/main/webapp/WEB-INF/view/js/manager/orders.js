$(document).ready(function () {
    var table = $('#ordsTable').DataTable({
        "bFilter":true,
        "ajax" : {
            "url": "/admin/get_orders",
            "type": "POST"
        },
        serverSide: true,
        columns: [
            { "data": "id", "name": "id",  "title": "#", "visible": true, "searchable": false},
            { "data": "opened", "name": "isOpened", "visible": false},
            { "data": null, "name": "openedTimeStamp", "title": "opened time", "render": function(data){
                return moment(data.openedTimeStamp).format('YYYY-MM-DD HH:mm');
            }},
            { "data": null, "name": "closedTimeStamp", "title": "closed time", "render": function(data){
                if (data.closedTimeStamp==null) return "-";
                return moment(data.closedTimeStamp).format('YYYY-MM-DD HH:mm');
            }},
            { "data": "table", "name": "table", "title": "table"},
            { "data": "dishesQuantity", "title": "dishes", "sortable": false, "searchable": false},
            { "data": "totalSum", "title": "total", "sortable": false},
            { "data": "waiterName", "name": "waiter", "title": "waiter"},

            { "data": null, "name": "fulfilled", "title": "isFulfilled", "sortable":false, "render": function(data){
                if (data.fulfilled) {
                    return '<p hidden="true">true</p><input type="checkbox" disabled="true" checked/>';
                } else {
                    return '<p hidden="true">false</p><input type="checkbox" disabled="true"/>';
                }
            }},
            { "data": null, "name": "isCancelled", "title": "isCancelled", "render": function(data){
                if (data.cancelled) {
                    return '<p hidden="true">true</p><input type="checkbox" disabled="true" checked/>';
                } else {
                    return '<p hidden="true">false</p><input type="checkbox" disabled="true"/>';
                }
            }},

            { "data": null, "sortable": false, "render": function(data){
                return '<a href="/waiter/edit_order?id=' + data.id + '">' +
                            '<input type="button" class="btn btn-default" value="Details"/>' +
                        '</a>';
            }
            }
        ]
    });
});

$(document).ready(function() {
    $('#ordsTable thead tr th.select-filter').each( function () {
        var title = $(this).text();
        $(this).html( '<input type="text" size="14" placeholder="Search '+title+'" />' );
    } );

    $('#ordsTable thead tr th.select-filter-table').each( function () {
        var title = $(this).text();
        $(this).html( '<input type="text"  size="2" placeholder="Table" />' );
    } );

    $('#ordsTable thead tr th.select-filter-waiter').each( function () {
        var title = $(this).text();
        $(this).html( '<input type="text" size="9" placeholder="Search '+title+'" />' );
    } );

    var table = $('#ordsTable').DataTable();
    table.columns().every( function () {
        var that = this;

        $( 'input', this.header() ).on( 'keyup change', function () {
            if ( that.search() !== this.value ) {
                that
                    .search( this.value )
                    .draw();
            }
        } );
    } );
} );
