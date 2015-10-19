var tapgo_path_polyline;
var tapgo_tap_marker, tapgo_set_marker;

function initTapGoMarkerPolyline() {
    tapgo_path_polyline = new google.maps.Polyline({
        geodesic : true,
        strokeColor : '#4EC3F5',
        strokeOpacity : 1.0,
        strokeWeight : 3,
        path : []
    });
}

function setTapGoMode(isTapGo) {
    isTapAndGoMode = isTapGo;
}

function setTapGoMarker(position) {
    if (tapgo_tap_marker) {
        tapgo_tap_marker.setPosition(position);
        tapgo_tap_marker.setMap(map);
    } else {
        tapgo_tap_marker = new MarkerWithLabel({
            position : position,
            icon : {
                url : 'ico_indicator_waypoint_tap.png',
                scaledSize : new google.maps.Size(40, 40),
                origin : new google.maps.Point(0, 0),
                anchor : new google.maps.Point(20, 20)
            },
            map : map
        });
    }
}

function setTapGoPath() {
    if (tapgo_set_marker) {
        tapgo_set_marker.setPosition(tapgo_tap_marker.position);
        tapgo_set_marker.setMap(map);
    } else {
        tapgo_set_marker = new MarkerWithLabel({
            position : tapgo_tap_marker.position,
            icon : {
                url : 'ico_indicator_waypoint_set.png',
                scaledSize : new google.maps.Size(40, 40),
                origin : new google.maps.Point(0, 0),
                anchor : new google.maps.Point(20, 20)
            },
            map : map
        });
    }
    tapgo_tap_marker.setMap(null);

    if (planning_drone_marker_arrow.position) {
        tapgo_path_polyline.getPath().setAt(0, planning_drone_marker_arrow.position);
        tapgo_path_polyline.getPath().setAt(1, tapgo_tap_marker.position);
        tapgo_path_polyline.setMap(map);
    }
}

function clearTapMarker() {
    if (tapgo_tap_marker) {
        tapgo_tap_marker.setMap(null);
    }
}

function clearTapGoSetMarker() {
    if (tapgo_set_marker) {
        tapgo_set_marker.setMap(null);
        if (planning_drone_marker_arrow.position) {
            tapgo_path_polyline.setMap(null);
            tapgo_path_polyline.path = [];
        }
    }
}

