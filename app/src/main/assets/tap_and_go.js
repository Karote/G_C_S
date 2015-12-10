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

function clearTapAndGoPlan() {
    if (tapgo_set_marker) {
        tapgo_set_marker.setMap(null);
        if (planning_drone_marker_arrow.position) {
            tapgo_path_polyline.setMap(null);
            tapgo_path_polyline.path = [];
        }
    }
}

function generateTapMarkerIcon(type, callback) {
    var imageWidth = imageHeight = 120;

    var svg = d3.select(document.createElement('div')).append('svg').attr('viewBox', '0 0 120 120').append('g');

    if (type == 'tap') {
        var defs = svg.append('svg:defs');
        var svg_1_blur = defs.append('filter').attr('id', 'svg_1_blur');
        var feGaussianBlur = svg_1_blur.append('feGaussianBlur').attr('stdDeviation', '2');
        var gradient = defs.append("svg:linearGradient").attr("id", "gradient").attr("x1", "0").attr("y1", "0").attr("x2", "0").attr("y2", "1");
        gradient.append("svg:stop").attr("offset", "0").attr("stop-color", "#ABDCFD");
        gradient.append("svg:stop").attr("offset", "1").attr("stop-color", "#3AB7E9");
        var svg_1 = svg.append('circle').attr('cx', '64').attr('cy', '64').attr('r', '47').attr('filter', 'url(#svg_1_blur)');
        var svg_2 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '50').attr('fill', '#fff');
        var svg_3 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '48').attr('fill', 'url(#gradient)');
        var svg_4 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '41').attr('fill', '#fff');
        var svg_5 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '37').attr('fill', 'url(#gradient)');
    } else {// 'set'
        var defs = svg.append('svg:defs');
        var svg_1_blur = defs.append('filter').attr('id', 'svg_1_blur').attr('width', '200%').attr('height', '200%').attr('y', '-50%').attr('x', '-50%');
        var feGaussianBlur = svg_1_blur.append('feGaussianBlur').attr('stdDeviation', '3');
        var gradient = defs.append("svg:linearGradient").attr("id", "gradient").attr("x1", "0").attr("y1", "0").attr("x2", "0").attr("y2", "1");
        gradient.append("svg:stop").attr("offset", "0").attr("stop-color", "#FDAD4B");
        gradient.append("svg:stop").attr("offset", "1").attr("stop-color", "#FD7C15");
        var svg_1 = svg.append('circle').attr('cx', '60').attr('cy', '62').attr('r', '33').attr('filter', 'url(#svg_1_blur)');
        var svg_2 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '35').attr('fill', '#fff');
        var svg_3 = svg.append('circle').attr('cx', '60').attr('cy', '60').attr('r', '25').attr('fill', 'url(#gradient)');
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
