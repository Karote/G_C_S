var polygon_vertices = [];
var polygon_polyline;
var polygon_polyline_end;
var footprints = [];
var footprint_rectangle;
var footprint_polyline;
var footprint_properties;
var infobubble;

function initSurvey() {

    var polygon_polyline_options = {
        strokeColor : '#5ea1c7',
        strokeOpacity : 1.0,
        strokeWeight : 3,
        clickable : false,
        map : map,
        zIndex : 255
    };
    polygon_polyline = new google.maps.Polyline(polygon_polyline_options);

    var lineSymbol = {
        strokeColor : '#5ea1c7',
        path : 'M 0,-1 0,1',
        strokeOpacity : 1,
        scale : 4
    };

    var polygon_polyline_end_options = {
        strokeOpacity : 0,
        icons : [{
            icon : lineSymbol,
            offset : '0',
            repeat : '20px'
        }],
        map : map,
        zIndex : 255
    };
    polygon_polyline_end = new google.maps.Polyline(polygon_polyline_end_options);

    var footprint_polyline_options = {
        strokeColor : '#17415c',
        strokeOpacity : 1,
        strokeWeight : 4,
        clickable : false,
        map : map,
        zIndex : 255,
    };
    footprint_polyline = new google.maps.Polyline(footprint_polyline_options);
    
    var infoOptions = {
        map: map,
        shadowStyle: 0,
        maxHeight: 72,
        padding: 6,
        backgroundColor: 'rgb(197,40,40)',
        borderRadius: 0,
        arrowSize: 10,
        borderWidth: 0,
        disableAutoPan: true,
        hideCloseButton: true,
        arrowPosition: 50,
        arrowStyle: 0
    };
    infobubble = new InfoBubble(infoOptions);
}

function initFootprintProperties(top_offset, left_offset, bottom_offset, right_offset) {
    footprint_properties = {
        top_left_lat_offset : top_offset,
        top_left_lng_offset : left_offset,
        bottom_right_lat_offset : bottom_offset,
        bottom_right_lng_offset : right_offset,
    }
}

function updatePolygon(polygonsInJson) {
    var vertices = JSON.parse(JSON.stringify(polygonsInJson));
    if (polygon_vertices.length > vertices.length) {
        console.log("123");
        clearPolygon();
    } else {
        if (polygon_polyline_end.getMap()) {
            polygon_polyline_end.setMap(null);
            polygon_polyline_end.setPath([]);
        }
    }
    for (var i = polygon_vertices.length; i < vertices.length; i++) {
        addPolygonVertex(vertices[i].latitude, vertices[i].longitude, i + 1);
    }

    if (!polygon_polyline.getMap()) {
        polygon_polyline.setMap(map);
    }

    if (vertices.length > 2) {
        polygon_polyline_end.getPath().push(new google.maps.LatLng(vertices[vertices.length - 1].latitude, vertices[vertices.length - 1].longitude));
        polygon_polyline_end.getPath().push(new google.maps.LatLng(vertices[0].latitude, vertices[0].longitude));
        polygon_polyline_end.setMap(map);
    }

}

function addPolygonVertex(lat, lng, index) {

    var polygon_vertex_marker_option = {
        position : new google.maps.LatLng(lat, lng),
        icon : {
            path : 'M26,7c10.5,0,19,8.5,19,19c0,10.5-8.5,19-19,19C15.5,45,7,36.5,7,26C7,15.5,15.5,7,26,7L26,7z',
            strokeColor : '#59c1ed',
            fillColor : '#59c1ed',
            fillOpacity : 1,
            anchor : new google.maps.Point(26, 26),
            scale : 1
        },
        clickable : true,
        crossOnDrag : false,
        draggable : true,
        opacity : 1,
        index : index,
        map : map
    };

    var polygon_vertex_marker = new google.maps.Marker(polygon_vertex_marker_option);
    
    google.maps.event.addListener(polygon_vertex_marker, 'dragstart', function(e) {
        if (infobubble.getContent()) {
            infobubble.close();
            infobubble.setContent(null);
        }
    });

    google.maps.event.addListener(polygon_vertex_marker, 'drag', function(e) {
        polygon_polyline.getPath().setAt(polygon_vertex_marker.index - 1, polygon_vertex_marker.getPosition());
        if (polygon_vertex_marker.index == 1) {
            if (polygon_polyline_end.getMap()) {
                polygon_polyline_end.getPath().setAt(1, polygon_vertex_marker.getPosition());
            }
        }
        if (polygon_vertex_marker.index == polygon_vertices.length) {
            if (polygon_polyline_end.getMap()) {
                polygon_polyline_end.getPath().setAt(0, polygon_vertex_marker.getPosition());
            }
        }
    });
    google.maps.event.addListener(polygon_vertex_marker, 'dragend', function(e) {
        AndroidFunction.onMapDragEndEvent(polygon_vertex_marker.index - 1, polygon_vertex_marker.getPosition().lat(), polygon_vertex_marker.getPosition().lng());
    });

    google.maps.event.addListener(polygon_vertex_marker, 'click', function(e) {
        showInfo(polygon_vertex_marker);
    });

    polygon_vertices.push(polygon_vertex_marker);
    polygon_polyline.getPath().push(new google.maps.LatLng(lat, lng));
    if (!polygon_polyline.getMap()) {
        polygon_polyline.setMap(map);
    }
}

function updateFootprint(footprintsInJson) {
    var footprints = JSON.parse(JSON.stringify(footprintsInJson));
    clearFootprint();
    for (var index in footprints) {
        addFootprint(footprints[index].latitude, footprints[index].longitude);
    }
}

function addFootprint(lat, lng) {

    if (footprint_properties) {
        var footprint = new google.maps.Polygon({
            strokeWeight : 0,
            fillColor : '#3978a9',
            fillOpacity : 0.3,
            paths : [{
                lat : lat + footprint_properties.top_left_lat_offset,
                lng : lng + footprint_properties.top_left_lng_offset
            }, {
                lat : lat + footprint_properties.bottom_right_lat_offset,
                lng : lng + footprint_properties.bottom_right_lng_offset
            }, {
                lat : lat - footprint_properties.top_left_lat_offset,
                lng : lng - footprint_properties.top_left_lng_offset
            }, {
                lat : lat - footprint_properties.bottom_right_lat_offset,
                lng : lng - footprint_properties.bottom_right_lng_offset
            }],
            map : map
        });
        footprints.push(footprint);
    }

    footprint_polyline.getPath().push(new google.maps.LatLng(lat, lng));
    if (!footprint_polyline.getMap()) {
        footprint_polyline.setMap(map);
    }
}

function clearSurvey() {
    clearPolygon();
    clearFootprint();
}

function clearPolygon() {
    for (var index in polygon_vertices) {
        polygon_vertices[index].setMap(null);
    }
    polygon_vertices = [];
    polygon_polyline.setMap(null);
    polygon_polyline.setPath([]);
    polygon_polyline_end.setMap(null);
    polygon_polyline_end.setPath([]);
}

function clearFootprint() {
    for (var index in footprints) {
        footprints[index].setMap(null);
    }
    footprints = [];
    footprint_polyline.setMap(null);
    footprint_polyline.setPath([]);
}

function setScopeMarkerDraggable(draggable) {
    for (var index in polygon_vertices) {
        polygon_vertices[index].setDraggable(draggable);
    }
}

function showInfo(markerObj) {
    if (infobubble.getContent()) {
        infobubble.close();
        infobubble.setContent(null);
        if (infobubble.getPosition() == markerObj.getPosition()) {
            return;
        }
    }
    infobubble.setContent(infoContent(markerObj));
    infobubble.open(map, markerObj);
}

function infoContent(markerObj) {
    var html = '<div class="infoSub_left" onclick="onClickInfoBubble(-1)">CLEAR ALL</div>' 
        + '<div class="infoSub_right" onclick="onClickInfoBubble(' + markerObj.index + ')">DELETE</div>';
    return html;
}

function onClickInfoBubble(deleteIndex){
    infobubble.close();
    infobubble.setContent(null);
    AndroidFunction.onMapDeleteMarker(deleteIndex);
}
