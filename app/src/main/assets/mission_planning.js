var planning_mission_marker_array = [];

function initPlanningMissionPolyline() {
    var mission_plan_polyline_options = {
        strokeColor : '#4EC3F5',
        strokeOpacity : 1.0,
        strokeWeight : 3,
        clickable : false,
        map : map
    };
    mission_plan_polyline = new google.maps.Polyline(mission_plan_polyline_options);
}


function updateMissionMarkers(missionInJson){
    var missions = JSON.parse(JSON.stringify(missionInJson));

    if(planning_mission_marker_array.length > missions.length){
        clearMissionPlanningMarkers();
    }

    for (var i = 0; i < missions.length; i++) {
        addMissionMarker(missions[i].latitude , missions[i].longitude , i + 1);
    }
}

function addMissionMarker(lat, lng, serial_number) {

    if(planning_mission_marker_array.length > serial_number){
            return;
    }
    var label_anchor_point_x = 4;
    if (serial_number > 9) {
        label_anchor_point_x = 8;
    }
    var planning_mission_marker_image = {
        url : 'ico_indicator_waypoint_circle.png',
        scaledSize : new google.maps.Size(40, 40),
        origin : new google.maps.Point(0, 0),
        anchor : new google.maps.Point(20, 20)
    };
    var planning_mission_marker = new MarkerWithLabel({
        position : new google.maps.LatLng(lat, lng),
        icon : planning_mission_marker_image,
        labelContent : serial_number,
        labelAnchor : new google.maps.Point(label_anchor_point_x, 8),
        labelClass : "mapIconLabel",
        labelInBackground : false,
        draggable : true,
        raiseOnDrag : false,
        zIndex : serial_number,
        map : map
    });
    planning_mission_marker_array.push(planning_mission_marker);
    google.maps.event.addListener(planning_mission_marker, 'click', function(e) {
        if (mapClickable) {
            google.maps.event.trigger(map, "click", e);
        }
    });
    google.maps.event.addListener(planning_mission_marker, 'mousedown', function(e) {
        var target = e.target || e.srcElement;
        if (target && target.className == 'mapIconLabel') {
            onLabel = true;
        }
        map.setOptions({
            draggable : false
        });
    });
    google.maps.event.addListener(planning_mission_marker, 'dragstart', function(e) {
        AndroidFunction.onMapDragStartEvent();
    });
    google.maps.event.addListener(planning_mission_marker, 'drag', function(e) {
        mission_plan_polyline.getPath().setAt(planning_mission_marker.labelContent - 1, planning_mission_marker.getPosition());
    });
    google.maps.event.addListener(planning_mission_marker, 'dragend', function(e) {
        AndroidFunction.onMapDragEndEvent(planning_mission_marker.labelContent - 1, planning_mission_marker.getPosition().lat(),
        planning_mission_marker
        .getPosition().lng());
    });

    var path = mission_plan_polyline.getPath();
    path.push(new google.maps.LatLng(lat, lng));
    mission_plan_polyline.setMap(map);
}

function clearMissionPlanningMarkers() {
    var i,
        j;
    for ( i = 0,
    j = planning_mission_marker_array.length; i < j; i++) {
        planning_mission_marker_array[i].setMap(null);
    }
    planning_mission_marker_array = [];

    mission_plan_polyline.setMap(null);
    mission_plan_polyline.setPath([]);
}

function fitMapShowAllMissionPlanning() {
    var i,
        j;
    var bounds = new google.maps.LatLngBounds();
    for ( i = 0,
    j = planning_mission_marker_array.length; i < j; i++) {
        bounds.extend(planning_mission_marker_array[i].position);
    }
    map.fitBounds(bounds);
}

/*
 function deleteSelectMarker(selected) {
 planning_mission_marker_array[selected].setMap(null);
 var path = mission_plan_polyline.getPath();
 path.removeAt(selected);
 planning_mission_marker_array.splice(selected, 1);
 mission_plan_polyline.setpath(path);
 mission_plan_polyline.setMap(map);
 }

 function hideMarkerOnMap() {
 var i,
 j;
 for ( i = 0,
 j = planning_mission_marker_array.length; i < j; i++) {
 planning_mission_marker_array[i].setMap(null);
 }
 mission_plan_polyline.setMap(null);
 }

 function showMarkerOnMap() {
 var i,
 j;
 for ( i = 0,
 j = planning_mission_marker_array.length; i < j; i++) {
 planning_mission_marker_array[i].setMap(map);
 }
 mission_plan_polyline.setMap(map);
 }

 function setMarkerShow(isMarkerShow) {
 var i,
 j,
 option;
 if (isMarkerShow) {
 option = map;
 } else {
 option = null;
 }
 for ( i = 0,
 j = planning_mission_marker_array.length; i < j; i++) {
 planning_mission_marker_array[i].setMap(option);
 }
 mission_plan_polyline.setMap(option);
 }
 */