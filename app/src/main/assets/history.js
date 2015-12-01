var history_mission_markers_array = [];
var history_flight_path = [];
var history_flight_path_polyline;

function initHistoryMarkerPolyline() {
    var history_droneMarker_outer = {
        path : 'M40,14.7c3.5,10.5-2.2,21.8-12.7,25.3C16.8,43.5,5.5,37.8,2,27.3S4.2,5.5,14.7,2C25.2-1.5,36.5,4.2,40,14.7L40,14.7z',
        strokeColor : '#000000',
        strokeOpacity : 0.4,
        fillColor : '#000000',
        fillOpacity : 0.4,
        anchor : new google.maps.Point(21, 21),
        scale : 1
    };

    var history_droneMarker_inner = {
        path : 'M30.5,17.9c1.7,5.2-1.1,10.9-6.4,12.6c-5.2,1.7-10.9-1.1-12.6-6.4c-1.7-5.2,1.1-10.9,6.4-12.6C23.1,9.8,28.8,12.6,30.5,17.9L30.5,17.9z',
        strokeColor : '#ffffff',
        fillColor : '#ffffff',
        fillOpacity : 1,
        anchor : new google.maps.Point(21, 21),
        scale : 1
    };

    var history_droneMarker_arrow = {
        path : 'M20.9,13L26,26.9c0,0-5-3.7-5.1-3.7c0,0-4.9,3.8-4.9,3.8L20.9,13L20.9,13z',
        strokeColor : '#ff8800',
        fillColor : '#ff8800',
        fillOpacity : 1,
        anchor : new google.maps.Point(21, 21),
        rotation : 0,
        scale : 1
    };

    history_flight_path_polyline = new google.maps.Polyline({
        path : history_flight_path,
        geodesic : true,
        strokeColor : '#4EC3F5',
        strokeOpacity : 0.5,
        strokeWeight : 6,
        icons : [{
            icon : history_droneMarker_outer,
            offset : '0%'
        }, {
            icon : history_droneMarker_inner,
            offset : '0%'
        }, {
            icon : history_droneMarker_arrow,
            offset : '0%'
        }]
    });

}

function loadHistory(historyMissionInJson, pathJson) {
    var i, j;

    var history_missions_data = JSON.parse(JSON.stringify(historyMissionInJson));
    var history_flight_path_data = JSON.parse("[" + pathJson + "]");

    clearHistoryMarkerPath();

    for ( i = 0, j = history_flight_path_data.length; i < j; i += 2) {
        history_flight_path.push(transLatLngToPosition(history_flight_path_data[i], history_flight_path_data[i + 1]));
    }

    if (history_flight_path.length > 1) {
        var startMarker = new MarkerWithLabel({
            position : history_flight_path[0],
            icon : {
                path : google.maps.SymbolPath.CIRCLE,
                scale : 8,
                strokeColor : '#000',
                fillColor : '#000',
                fillOpacity : 1.0
            },
            labelAnchor : new google.maps.Point(4, 8),
            labelContent : 'S',
            labelClass : "greenMarkerLabel",
            labelInBackground : false,
            map : map
        });
        history_mission_markers_array.push(startMarker);

        var endMarker = new MarkerWithLabel({
            position : history_flight_path[history_flight_path.length - 1],
            icon : {
                path : google.maps.SymbolPath.CIRCLE,
                scale : 8,
                strokeColor : '#000',
                fillColor : '#000',
                fillOpacity : 1.0
            },
            labelAnchor : new google.maps.Point(4, 8),
            labelContent : 'E',
            labelClass : "redMarkerLabel",
            labelInBackground : false,
            map : map
        });
        history_mission_markers_array.push(endMarker);
    }

    for ( i = 0, j = history_missions_data.length; i < j; i++) {
        var lat = history_missions_data[i].latitude;
        var lng = history_missions_data[i].longitude;
        var type = history_missions_data[i].type;

        addHistoryMissionMarker(lat, lng, i + 1, type);
    }

    history_flight_path_polyline.setPath(history_flight_path);
    history_flight_path_polyline.setMap(map);

    var flightbounds = new google.maps.LatLngBounds();
    for ( i = 0, j = history_flight_path.length; i < j; i++) {
        flightbounds.extend(history_flight_path[i]);
    }
    map.fitBounds(flightbounds);

    var lengthInMeters = google.maps.geometry.spherical.computeLength(history_flight_path_polyline.getPath());
    AndroidFunction.onMapPolylineLengthCalculated(lengthInMeters);

    animateDrone();
}

var start_animate;
function animateDrone() {
    var count = 0;
    start_animate = window.setInterval(function() {
        count = (count + 1) % 200;

        var icons = history_flight_path_polyline.get('icons');
        icons[0].offset = (count / 2) + '%';
        icons[1].offset = (count / 2) + '%';
        icons[2].offset = (count / 2) + '%';
        history_flight_path_polyline.set('icons', icons);
    }, 20);
}

function clearHistoryMarkerPath() {
    window.clearInterval(start_animate);

    history_flight_path_polyline.setMap(null);
    history_flight_path_polyline.setPath([]);
    history_flight_path = [];

    var i, j;
    for ( i = 0, j = history_mission_markers_array.length; i < j; i++) {
        history_mission_markers_array[i].setMap(null);
    }
    history_mission_markers_array = [];
}

function addHistoryMissionMarker(lat, lng, serial_number, marker_type) {
    var mission_marker = new google.maps.Marker({
        position : new google.maps.LatLng(lat, lng),
        draggable : false,
        raiseOnDrag : false,
        zIndex : serial_number,
        markerType : marker_type,
        map : map
    });
    generatePlanningMissionMarkerIcon(marker_type, serial_number, function(src) {
        var mission_marker_image = {
            url : src,
            scaledSize : new google.maps.Size(38, 38),
            anchor : new google.maps.Point(19, 19)
        };
        mission_marker.setIcon(mission_marker_image);
    });
    history_mission_markers_array.push(mission_marker);
}
