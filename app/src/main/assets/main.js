var map;
var GeoMarker, planning_drone_marker_outer, planning_drone_marker_inner, planning_drone_marker_arrow;
var mapClickable, isTapAndGoMode;
var mission_plan_polyline;
var droneHeading_old = 0;
var onLabel = false;

function initialize() {

    initMap();
    initPlanningDroneMarker();
    initPlanningMissionPolyline();
    initHistoryMarkerPolyline();
    initTapGoMarkerPolyline();
    initSurvey();
    AndroidFunction.onMapLoaded();
}

function initMap() {
    var initial_location;
    initial_location = new google.maps.LatLng(24.712299, 120.916408);
    // 中強光電竹南廠

    var map_style_array = [{
        featureType : "poi",
        elementType : "all",
        stylers : [{
            visibility : "off"
        }]
    }, {
        featureType : "transit",
        elementType : "all",
        stylers : [{
            visibility : "off"
        }]
    }];

    var map_options = {
        center : initial_location,
        zoom : 17,
        mapTypeId : google.maps.MapTypeId.ROADMAP,
        disableDefaultUI : true,
        styles : map_style_array
    };
    map = new google.maps.Map(document.getElementById("map"), map_options);

    google.maps.event.addListener(map, 'click', mapClickListener);
    google.maps.event.addListener(map, 'mousedown', function(e) {
        onLabel = false;
        map.setOptions({
            draggable : true
        });
    });

    google.maps.event.addListener(map, 'bounds_changed', function() {
        initializeNoWayPointZone(map.getBounds());
    });
    var GeoMarkerOption = {
        icon : {
            path : google.maps.SymbolPath.CIRCLE,
            scale : 10,
            strokeColor : '#34a7ff',
            strokeOpacity : 1,
            fillColor : '#34a7ff',
            fillOpacity : 1
        }
    }
    GeoMarker = new GeolocationMarker(map, GeoMarkerOption);
    GeoMarker.setCircleOptions({
        fillColor : '#34a7ff',
        strokeColor : '#34a7ff',
        strokeOpacity : 1,
        strokeWeight : 2
    });

}

function enableGeoLocation(enable) {
    if (!GeoMarker) { return; }
    if (enable) {
        GeoMarker.setMap(map);
    } else {
        GeoMarker.setMap(null);
    }
}

function initPlanningDroneMarker() {
    var planning_drone_marker_outerOption = {
        icon : {
            path : 'M40,14.7c3.5,10.5-2.2,21.8-12.7,25.3C16.8,43.5,5.5,37.8,2,27.3S4.2,5.5,14.7,2C25.2-1.5,36.5,4.2,40,14.7L40,14.7z',
            strokeColor : '#000000',
            strokeOpacity : 0.4,
            fillColor : '#000000',
            fillOpacity : 0.4,
            anchor : new google.maps.Point(21, 21),
            scale : 1
        },
        zIndex : google.maps.Marker.MAX_ZINDEX + 1,
        clickable : false,
        map : map
    };

    var planning_drone_marker_innerOption = {
        icon : {
            path : 'M30.5,17.9c1.7,5.2-1.1,10.9-6.4,12.6c-5.2,1.7-10.9-1.1-12.6-6.4c-1.7-5.2,1.1-10.9,6.4-12.6C23.1,9.8,28.8,12.6,30.5,17.9L30.5,17.9z',
            strokeColor : '#ffffff',
            fillColor : '#ffffff',
            fillOpacity : 1,
            anchor : new google.maps.Point(21, 21),
            scale : 1
        },
        zIndex : google.maps.Marker.MAX_ZINDEX + 2,
        clickable : false,
        map : map
    };

    var planning_drone_marker_arrowOption = {
        icon : {
            path : 'M20.9,13L26,26.9c0,0-5-3.7-5.1-3.7c0,0-4.9,3.8-4.9,3.8L20.9,13L20.9,13z',
            strokeColor : '#ff8800',
            fillColor : '#ff8800',
            fillOpacity : 1,
            anchor : new google.maps.Point(21, 21),
            rotation : 0,
            scale : 1
        },
        zIndex : google.maps.Marker.MAX_ZINDEX + 3,
        clickable : false,
        map : map
    };

    planning_drone_marker_outer = new google.maps.Marker(planning_drone_marker_outerOption);
    planning_drone_marker_inner = new google.maps.Marker(planning_drone_marker_innerOption);
    planning_drone_marker_arrow = new google.maps.Marker(planning_drone_marker_arrowOption);
}

function changeMapType() {
    if (map.getMapTypeId() == google.maps.MapTypeId.ROADMAP) {
        map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
    } else if (map.getMapTypeId() == google.maps.MapTypeId.SATELLITE) {
        map.setMapTypeId(google.maps.MapTypeId.HYBRID);
    } else {
        map.setMapTypeId(google.maps.MapTypeId.ROADMAP);
    }
}

function mapClickListener(event) {
    if (onLabel) {
        onLabel = false;
        return;
    }
    if (mapClickable) {
        AndroidFunction.onMapClickEvent(event.latLng.lat(), event.latLng.lng());
        // addMissionMarker(event.latLng.lat(), event.latLng.lng(), mission_plan_polyline.getPath().getLength() + 1, 'LAND');
    }
    if (isTapAndGoMode) {
        setTapGoMarker(event.latLng);
    }
    
    if (infobubble.getContent()) {
        infobubble.close();
        infobubble.setContent(null);
    }
}

function transLatLngToPosition(lat, lng) {
    var pos = new google.maps.LatLng(lat / 10000000, lng / 10000000, true);
    return pos;
}

function setMapClickable(isClickable) {
    mapClickable = isClickable;
}

function setMapToMyLocation() {
    var currPosition = null;

    currPosition = GeoMarker.getPosition();
    if (currPosition != null) {
        map.setCenter(currPosition);
    } else {
        AndroidFunction.onWarningMessage("無法取得現在位置");
    }
    if (map.getZoom() < 17) {
        map.setZoom(17);
    }
}

function setMapTo(setToLat, setToLng) {
    var setToPos = transLatLngToPosition(setToLat, setToLng);
    map.setCenter(setToPos);
}

function updateDroneLocation(droneLat, droneLng, heading) {

    var dronePos = transLatLngToPosition(droneLat, droneLng);
    planning_drone_marker_outer.setPosition(dronePos);
    planning_drone_marker_inner.setPosition(dronePos);
    planning_drone_marker_arrow.setPosition(dronePos);
    planning_drone_marker_arrow.icon.rotation = heading;
    planning_drone_marker_arrow.setMap(map);
    if (tapgo_path_polyline) {
        tapgo_path_polyline.getPath().setAt(0, dronePos);
    }
}

function clearDroneMarker() {
    planning_drone_marker_outer.setMap(null);
    planning_drone_marker_inner.setMap(null);
    planning_drone_marker_arrow.setMap(null);
}

function fitMapShowDroneAndMe() {
    var droneBound = new google.maps.LatLngBounds();
    droneBound.extend(droneMarkerOuter.position);
    droneBound.extend(GeoMarker.getPosition());

    map.fitBounds(droneBound);
}

function mapClean() {
    clearMissionPlanningMarkers();
    clearTapMarker();
    clearTapGoSetMarker();
    clearHistoryMarkerPath();
    clearSurvey();
}
