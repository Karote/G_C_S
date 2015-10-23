var polygon_vertices = [];
var polygon_polyline;
var polygon_polyline_end;
var polygon_vertex_marker_image;
var footprints = [];
var footprint_rectangle;
var footprint_polyline;
var footprint_properties;
var aerial_survey_missions = [];
var aerial_survey_missions_polyline;

function initSurvey() {

    polygon_vertex_marker_image = {
        url : 'ico_indicator_waypoint_circle.png',
        scaledSize : new google.maps.Size(40, 40),
        origin : new google.maps.Point(0, 0),
        anchor : new google.maps.Point(20, 20)
    };

    var polygon_polyline_options = {
        strokeColor : '#4EC3F5',
        strokeOpacity : 1.0,
        strokeWeight : 3,
        clickable : false,
        map : map,
        zIndex : 255
    };
    polygon_polyline = new google.maps.Polyline(polygon_polyline_options);

    var lineSymbol = {
        path : 'M 0,-1 0,1',
        strokeOpacity : 1,
        scale : 4,
        fillColor : '#4EC3F5'
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

    aerial_survey_missions_polyline = new google.maps.Polyline(polygon_polyline_options);

    var footprint_polyline_options = {
        strokeColor : '#000000',
        strokeOpacity : 0.8,
        strokeWeight : 3,
        clickable : false,
        map : map,
        zIndex : 255,
    };
    footprint_polyline = new google.maps.Polyline(footprint_polyline_options);

    footprint_rectangle = new google.maps.Polygon({
        strokeColor : '#333333',
        strokeOpacity : 0.3,
        strokeWeight : 2,
        fillColor : '#FF0000',
        fillOpacity : 0.35
    });

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
        if (polygon_polyline.getPath().length > 0) {
            polygon_polyline.getPath().pop();
        }
    }
    for (var i = polygon_vertices.length; i < vertices.length; i++) {
        addPolygonVertex(vertices[i].latitude, vertices[i].longitude, i + 1);
    }

    polygon_polyline.getPath().push(new google.maps.LatLng(vertices[0].latitude, vertices[0].longitude));
    if (!polygon_polyline.getMap()) {
        polygon_polyline.setMap(map);
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
    
    google.maps.event.addListener(polygon_vertex_marker, 'drag', function(e) {
        polygon_polyline.getPath().setAt(polygon_vertex_marker.index - 1, polygon_vertex_marker.getPosition());
    });
    google.maps.event.addListener(polygon_vertex_marker, 'dragend', function(e) {
        AndroidFunction.onMapDragEndEvent(polygon_vertex_marker.index - 1, polygon_vertex_marker.getPosition().lat(), polygon_vertex_marker.getPosition().lng());
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
            strokeColor : '#00CCCC',
            strokeOpacity : 0.8,
            strokeWeight : 2,
            fillColor : '#009999',
            fillOpacity : 0.35,
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
}

function clearFootprint() {
    for (var index in footprints) {
        footprints[index].setMap(null);
    }
    footprints = [];
    footprint_polyline.setMap(null);
    footprint_polyline.setPath([]);
}
