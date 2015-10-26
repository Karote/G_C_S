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

    tapgo_tap_marker = new google.maps.Marker({
        map : map
    });
    generateTapMarkerIcon('tap', function(src) {
        var marker_image = {
            url : src,
            scaledSize : new google.maps.Size(48, 48),
            anchor : new google.maps.Point(24, 24)
        };
        tapgo_tap_marker.setIcon(marker_image);
    });

    tapgo_set_marker = new google.maps.Marker({
        map : map
    });
    generateTapMarkerIcon('set', function(src) {
        var marker_image = {
            url : src,
            scaledSize : new google.maps.Size(48, 48),
            anchor : new google.maps.Point(24, 24)
        };
        tapgo_set_marker.setIcon(marker_image);
    });
}

function setTapGoMode(isTapGo) {
    isTapAndGoMode = isTapGo;
}

function setTapGoMarker(position) {
    tapgo_tap_marker.setPosition(position);
    tapgo_tap_marker.setMap(map);
}

function setTapGoPath() {
    tapgo_set_marker.setPosition(tapgo_tap_marker.position);
    tapgo_set_marker.setMap(map);
    clearTapMarker();

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

function generateTapMarkerIcon(type, callback) {
    var imageWidth = imageHeight = 52;

    var svg = d3.select(document.createElement('div')).append('svg').attr('viewBox', '0 0 52 52').append('g');

    if (type == 'tap') {
        var SVGID_1 = svg.append('path').attr('d', 'M10.4,41.6C6.3,37.4,4,31.9,4,26s2.3-11.4,6.4-15.6C14.6,6.3,20.1,4,26,4c5.9,0,11.4,2.3,15.6,6.4C45.7,14.6,48,20.1,48,26s-2.3,11.4-6.4,15.6C37.4,45.7,31.9,48,26,48C20.1,48,14.6,45.7,10.4,41.6').attr('fill', '#FFFFFF');
        var SVGID_2 = svg.append('path').attr('d', 'M26,5c11.6,0,21,9.4,21,21s-9.4,21-21,21C14.4,47,5,37.6,5,26S14.4,5,26,5L26,5z').attr('fill', '#59c1ed');
        var SVGID_3 = svg.append('path').attr('d', 'M26,8c9.9,0,18,8.1,18,18s-8.1,18-18,18C16.1,44,8,36,8,26S16.1,8,26,8L26,8z').attr('fill', '#ffffff');
        var SVGID_4 = svg.append('path').attr('d', 'M10,26c0-8.8,7.2-16,16-16c8.8,0,16,7.2,16,16s-7.2,16-16,16C17.2,42,10,34.8,10,26').attr('fill', '#59c1ed');
    } else {// 'set'
        var SVGID_1 = svg.append('path').attr('d', 'M26,11c8.3,0,15,6.7,15,15c0,8.3-6.7,15-15,15c-8.3,0-15-6.7-15-15C11,17.7,17.7,11,26,11L26,11z').attr('fill', '#ffffff');
        var gradient = svg.append("svg:defs").append("svg:linearGradient").attr("id", "gradient").attr("x1", "0%").attr("y1", "0%").attr("x2", "0%").attr("y2", "100%").attr("spreadMethod", "pad");
        gradient.append("svg:stop").attr("offset", "0%").attr("stop-color", "#FDAD4B").attr("stop-opacity", 1);
        gradient.append("svg:stop").attr("offset", "100%").attr("stop-color", "#FD7C15").attr("stop-opacity", 1);
        var SVGID_2 = svg.append('path').attr('d', 'M15,26c0-6.1,4.9-11,11-11c6.1,0,11,4.9,11,11c0,6.1-4.9,11-11,11C19.9,37,15,32.1,15,26').attr('fill', 'url(#gradient)');
    }

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
