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

function updateMissionMarkers(missionInJson) {
    var missions = JSON.parse(JSON.stringify(missionInJson));
    if (missions.length == 0) {
        clearMissionPlanningMarkers();
        return;
    }

    while (planning_mission_marker_array.length > missions.length) {
        planning_mission_marker_array.pop().setMap(null);
        mission_plan_polyline.getPath().pop();
    }

    for (var i = 0; i < missions.length; i++) {
        addMarkerAndPath(missions[i], i + 1);
    }
}

function addMarkerAndPath(mission, index) {
    if (tryToAddMarker(mission, index)) {
        addPath(mission, index);
    }
}

function addPath(mission_marker_to, index) {
    if (!mission_marker_to) { return; }

    if (!mission_plan_polyline.getMap()) {
        mission_plan_polyline.setMap(map);
    }
    var path = mission_plan_polyline.getPath();

    if (path.length >= index) {
        path.setAt(index - 1, new google.maps.LatLng(mission_marker_to.latitude, mission_marker_to.longitude))
    } else {
        path.push(new google.maps.LatLng(mission_marker_to.latitude, mission_marker_to.longitude));
    }
}

function tryToAddMarker(mission, index) {
    if (!mission) { return false; }
    if (planning_mission_marker_array.length < index) {
        addMissionMarker(mission.latitude, mission.longitude, index, mission.type);
        return true;
    }
    var existed_mission = planning_mission_marker_array[index - 1];
    var googleLanLng = new google.maps.LatLng(mission.latitude, mission.longitude);
    if (existed_mission.position.lat() == googleLanLng.lat() && existed_mission.position.lng() == googleLanLng.lng() && existed_mission.markerType == mission.type) {
        return false;
    }
    existed_mission.position = new google.maps.LatLng(mission.latitude, mission.longitude);
    existed_mission.setMap(null);
    if (existed_mission.type != mission.type) {
        generatePlanningMissionMarkerIcon(mission.type, index, function(src) {
            var marker_image = {
                url : src,
                scaledSize : new google.maps.Size(38, 38),
                anchor : new google.maps.Point(19, 19)
            };
            existed_mission.setIcon(marker_image);
            existed_mission.markerType = mission.type;
        });
    }
    existed_mission.setMap(map);
    return true;
}

function addMissionMarker(lat, lng, serial_number, marker_type) {
    var planning_mission_marker = new google.maps.Marker({
        position : new google.maps.LatLng(lat, lng),
        draggable : true,
        raiseOnDrag : false,
        zIndex : serial_number,
        markerType : marker_type,
        map : map
    });
    generatePlanningMissionMarkerIcon(marker_type, serial_number, function(src) {
        var planning_mission_marker_image = {
            url : src,
            scaledSize : new google.maps.Size(38, 38),
            anchor : new google.maps.Point(19, 19)
        };
        planning_mission_marker.setIcon(planning_mission_marker_image);
    });
    planning_mission_marker_array.push(planning_mission_marker);
    google.maps.event.addListener(planning_mission_marker, 'click', function(e) {
        if (mapClickable) {
            google.maps.event.trigger(map, "click", e);
        }
    });
    google.maps.event.addListener(planning_mission_marker, 'mousedown', function(e) {
        map.setOptions({
            draggable : false
        });
    });
    google.maps.event.addListener(planning_mission_marker, 'dragstart', function(e) {
        AndroidFunction.onMapDragStartEvent();
    });
    google.maps.event.addListener(planning_mission_marker, 'drag', function(e) {
        mission_plan_polyline.getPath().setAt(planning_mission_marker.zIndex - 1, planning_mission_marker.getPosition());
    });
    google.maps.event.addListener(planning_mission_marker, 'dragend', function(e) {
        AndroidFunction.onMapDragEndEvent(planning_mission_marker.zIndex - 1, planning_mission_marker.getPosition().lat(), planning_mission_marker.getPosition().lng());
    });

}

function clearMissionPlanningMarkers() {
    var i, j;
    for (i = 0, j = planning_mission_marker_array.length; i < j; i++) {
        planning_mission_marker_array[i].setMap(null);
    }
    planning_mission_marker_array = [];
    if (mission_plan_polyline) {
        mission_plan_polyline.setMap(null);
        mission_plan_polyline.setPath([]);
    }
}

function fitMapShowAllMissionPlanning() {
    var i, j;
    var bounds = new google.maps.LatLngBounds();
    for (i = 0, j = planning_mission_marker_array.length; i < j; i++) {
        bounds.extend(planning_mission_marker_array[i].position);
    }
    map.fitBounds(bounds);
}

function generatePlanningMissionMarkerIcon(markerType, number, callback) {
    var fontSize = 16, imageWidth = imageHeight = 52;

    var svg = d3.select(document.createElement('div')).append('svg').attr('viewBox', '0 0 52 52').append('g');

    var dy;
    switch(markerType) {
    case 'TAKEOFF':
        var SVGID_1 = svg.append('path').attr('d', 'M4,45c-0.4,0-0.7-0.2-0.9-0.5C3,44.3,3,44.2,3,44c0-0.2,0-0.3,0.1-0.5l22-38C25.3,5.2,25.6,5,26,5c0.4,0,0.7,0.2,0.9,0.5l22,38C49,43.7,49,43.8,49,44c0,0.2,0,0.3-0.1,0.5C48.7,44.8,48.4,45,48,45H4').attr('fill', '#FFFFFF');
        var SVGID_2 = svg.append('polygon').attr('points', '26,6 48,44 4,44 26,6').attr('fill', '#59c1ed');
        dy = 34;
        break;
    case 'LAND':
        var SVGID_1 = svg.append('path').attr('d', 'M25.1,45.5l-22-38C3,7.4,3,7.2,3,7c0-0.2,0-0.3,0.1-0.5C3.3,6.2,3.6,6,4,6h44c0.4,0,0.7,0.2,0.9,0.5C49,6.7,49,6.8,49,7c0,0.2,0,0.3-0.1,0.5l-22,38C26.7,45.8,26.4,46,26,46C25.6,46,25.3,45.8,25.1,45.5').attr('fill', '#FFFFFF');
        var SVGID_2 = svg.append('polygon').attr('points', '26,45 4,7 48,7 26,45').attr('fill', '#59c1ed');
        dy = 26;
        break;
    case 'WAY_POINT':
    default:
        var SVGID_1 = svg.append('path').attr('d', 'M6.8,25.8C6.8,14.9,15.6,6,26.4,6c10.9,0,19.7,8.9,19.7,19.8c0,10.9-8.8,19.8-19.7,19.8C15.6,45.6,6.8,36.7,6.8,25.8').attr('fill', '#FFFFFF');
        var SVGID_2 = svg.append('path').attr('d', 'M26.4,7c10.3,0,18.7,8.4,18.7,18.8c0,10.4-8.4,18.8-18.7,18.8c-10.3,0-18.7-8.4-18.7-18.8C7.7,15.4,16.1,7,26.4,7L26.4,7z').attr('fill', '#59c1ed');
        dy = 30;
        break;
    }

    var text = svg.append('text').attr('dx', 26).attr('dy', dy).attr('text-anchor', 'middle').attr('style', 'font-size:' + fontSize + 'px; fill: #FFFFFF; font-family: Arial, Verdana; font-weight: bold').text(number);

    var svgNode = svg.node().parentNode.cloneNode(true), image = new Image();

    d3.select(svgNode).select('clippath').remove();

    var xmlSource = (new XMLSerializer()).serializeToString(svgNode);

    image.onload = (function(imageWidth, imageHeight) {
        var canvas = document.createElement('canvas'), context = canvas.getContext('2d'), dataURL;

        d3.select(canvas).attr('width', imageWidth).attr('height', imageHeight);

        context.drawImage(image, 0, 0, imageWidth, imageHeight);

        dataURL = canvas.toDataURL();

        callback(dataURL);
    }).bind(this, imageWidth, imageHeight);

    image.src = 'data:image/svg+xml;base64,' + btoa(encodeURIComponent(xmlSource).replace(/%([0-9A-F]{2})/g, function(match, p1) {
        return String.fromCharCode('0x' + p1);
    }));
}