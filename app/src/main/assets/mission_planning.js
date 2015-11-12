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
    var fontSize = 48, imageWidth = imageHeight = 120;

    var svg = d3.select(document.createElement('div')).append('svg').attr('viewBox', '0 0 120 120').append('g');

    var defs = svg.append('svg:defs');
    var svg_1_blur = defs.append('filter').attr('id', 'svg_1_blur');
    var feGaussianBlur = svg_1_blur.append('feGaussianBlur').attr('stdDeviation', '2');
    var gradient = defs.append("svg:linearGradient").attr("id", "gradient").attr("x1", "0").attr("y1", "0").attr("x2", "0").attr("y2", "1");
    gradient.append("svg:stop").attr("offset", "0").attr("stop-color", "#ABDCFD");
    gradient.append("svg:stop").attr("offset", "1").attr("stop-color", "#3AB7E9");

    var dy;
    switch(markerType) {
    case 'TAKEOFF':
        var svg_1 = svg.append('path').attr('d', 'm4,45c-0.4,0 -0.7,-0.2 -0.9,-0.5c-0.1,-0.2 -0.1,-0.3 -0.1,-0.5c0,-0.2 0,-0.3 0.1,-0.5l22,-38c0.2,-0.3 0.5,-0.5 0.9,-0.5c0.4,0 0.7,0.2 0.9,0.5l22,38c0.1,0.2 0.1,0.3 0.1,0.5c0,0.2 0,0.3 -0.1,0.5c-0.2,0.3 -0.5,0.5 -0.9,0.5h-44').attr('transform', 'scale(2.2) translate(3,3)').attr('filter', 'url(#svg_1_blur)');
        var svg_2 = svg.append('path').attr('d', 'm4,45c-0.4,0 -0.7,-0.2 -0.9,-0.5c-0.1,-0.2 -0.1,-0.3 -0.1,-0.5c0,-0.2 0,-0.3 0.1,-0.5l22,-38c0.2,-0.3 0.5,-0.5 0.9,-0.5c0.4,0 0.7,0.2 0.9,0.5l22,38c0.1,0.2 0.1,0.3 0.1,0.5c0,0.2 0,0.3 -0.1,0.5c-0.2,0.3 -0.5,0.5 -0.9,0.5h-44').attr('transform', 'scale(2.3)').attr('fill', '#fff');
        var svg_3 = svg.append('polygon').attr('points', '26,6 48,44 4,44 26,6').attr('transform', 'scale(2.3)').attr('fill', 'url(#gradient)');
        dy = 88;
        break;
    case 'LAND':
        var svg_1 = svg.append('path').attr('d', 'm25.1,45.5l-22,-38c-0.1,-0.1 -0.1,-0.3 -0.1,-0.5c0,-0.2 0,-0.3 0.1,-0.5c0.2,-0.3 0.5,-0.5 0.9,-0.5h44c0.4,0 0.7,0.2 0.9,0.5c0.1,0.2 0.1,0.3 0.1,0.5c0,0.2 0,0.3 -0.1,0.5l-22,38c-0.2,0.3 -0.5,0.5 -0.9,0.5c-0.4,0 -0.7,-0.2 -0.9,-0.5').attr('transform', 'scale(2.2) translate(3,3)').attr('filter', 'url(#svg_1_blur)');
        var svg_2 = svg.append('path').attr('d', 'm25.1,45.5l-22,-38c-0.1,-0.1 -0.1,-0.3 -0.1,-0.5c0,-0.2 0,-0.3 0.1,-0.5c0.2,-0.3 0.5,-0.5 0.9,-0.5h44c0.4,0 0.7,0.2 0.9,0.5c0.1,0.2 0.1,0.3 0.1,0.5c0,0.2 0,0.3 -0.1,0.5l-22,38c-0.2,0.3 -0.5,0.5 -0.9,0.5c-0.4,0 -0.7,-0.2 -0.9,-0.5').attr('transform', 'scale(2.3)').attr('fill', '#fff');
        var svg_3 = svg.append('polygon').attr('points', '26,45 4,7 48,7 26,45').attr('transform', 'scale(2.3)').attr('fill', 'url(#gradient)');
        dy = 62;
        break;
    case 'CAMERA_TRIGGER_DISTANCE':
        var svg_1 = svg.append('path').attr('d', 'm23.5,105c-9.1,0 -16.5,-7.4 -16.5,-16.5v-42.6c0,-9.1 7.4,-16.5 16.5,-16.5h3.5c1.1,0 2,-0.9 2,-2v-6.2c0,-0.6 0.5,-1.1 1.1,-1.1h27.8c0.6,0 1.1,0.5 1.1,1.1v6.2c0,1.1 0.9,2 2,2h39.5c9.1,0 16.5,7.4 16.5,16.5v42.7c0,9.1 -7.4,16.5 -16.5,16.5h-77').attr('filter', 'url(#svg_1_blur)');
        var svg_2 = svg.append('path').attr('d', 'm96.5,103h-77c-10.2,0 -18.5,-8.3 -18.5,-18.5v-42.6c0,-10.2 8.3,-18.5 18.5,-18.5h77c10.2,0 18.5,8.3 18.5,18.5v42.7c-0.1,10.2 -8.3,18.4 -18.5,18.4l0,0zm-42.6,-67.8h-27.8c-1.7,0 -3.1,-1.4 -3.1,-3.1v-14.9c0,-1.7 1.4,-3.1 3.1,-3.1h27.8c1.7,0 3.1,1.4 3.1,3.1v14.8c0,1.8 -1.4,3.2 -3.1,3.2l0,0z').attr('fill', '#fff');
        var svg_3 = svg.append('path').attr('d', 'm19.5,101c-9.1,0 -16.5,-7.4 -16.5,-16.5v-42.6c0,-9.1 7.4,-16.5 16.5,-16.5h3.5c1.1,0 2,-0.9 2,-2v-6.2c0,-0.6 0.5,-1.1 1.1,-1.1h27.8c0.6,0 1.1,0.5 1.1,1.1v6.2c0,1.1 0.9,2 2,2h39.5c9.1,0 16.5,7.4 16.5,16.5v42.7c0,9.1 -7.4,16.5 -16.5,16.5h-77').attr('fill', 'url(#gradient)');
        dy = 80;
        break;
    case 'WAY_POINT':
    default:
        var svg_1 = svg.append('circle').attr('cx', '64').attr('cy', '64').attr('r', '46').attr('filter', 'url(#svg_1_blur)');
        var svg_2 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '46').attr('fill', '#fff');
        var svg_3 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '44').attr('fill', 'url(#gradient)');
        dy = 76;
        break;
    }

    var text = svg.append('text').attr('dx', 60).attr('dy', dy).attr('text-anchor', 'middle').attr('style', 'font-size:' + fontSize + 'px; fill: #FFFFFF; font-family: sans-serif; font-weight: bold').text(number);

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