category_a_name = 'Category A';
category_b_name = 'Category B';
category_city_limit_name = 'NO Fly';
opened_info = null;
loadedLatLngBounds = new google.maps.LatLngBounds();

category_a_icon = {
        url : 'category_a_marker.png',
        scaledSize : new google.maps.Size(40, 68),
        origin : new google.maps.Point(0, 0),
        anchor : new google.maps.Point(20, 68)
    };

category_b_icon = {
        url : 'category_b_marker.png',
        scaledSize : new google.maps.Size(40, 68),
        origin : new google.maps.Point(0, 0),
        anchor : new google.maps.Point(20, 68)
    };

function isCategoryA(category_code) {
    return category_code == 'A';
}

function isCategoryB(category_code) {
    return category_code == 'B';
}

function isCategoryCityLimit(category_code) {
    return category_code == 'city_limit';
}

function categoryName(category_code) {
    if (isCategoryA(category_code)) {
        return category_a_name;
    } else if (isCategoryB(category_code)) {
        return category_b_name;
    } else {
        return category_city_limit_name;
    }
}

function showCityLimitCircle(map, item, infowindow) {

    var options = {
        strokeColor: '#FF0000',
        strokeOpacity: 0.6,
        strokeWeight: 2,
        fillColor: '#FF0000',
        fillOpacity: 0.2,
        map: map,
        center: marker_latlng,
        radius: item[5] * 1000
    };
    var cityCircle = new google.maps.Circle(options);
    google.maps.event.addListener(cityCircle, 'click', function () {
        if (opened_info) {
            opened_info.close();
        }
        opened_info = infowindow;
        infowindow.open(map, marker);
    });

}


function show(map, item, category) {

    title = item[2] + " (" + categoryName(category) + ")";
    if (isCategoryA(category)) {
        title += "<br/><small>[Red circle] No Fly Zone<br/>[Yellow circle] Height Restriction Zone</small>"
    } else {
        title += "<br/><small>[Red circle] No Fly Zone</small>"
    }
    var infowindow = new google.maps.InfoWindow({
        content: title
    });

    if (isCategoryCityLimit()) {
        showCityLimitCircle(map, item, infowindow);
        return;
    }

    color = '#f39c12';
    mile = 5; // 5 miles
    image = category_a_icon;
    if (isCategoryB(category)) {
        color = '#2d89ef';
        mile = 1;
        image = category_b_icon;
    }

    var marker_latlng = new google.maps.LatLng(item[0], item[1]);
    var marker = new google.maps.Marker({
        position: marker_latlng,
        popup: true,
        icon: image,
        map: map
    });

    // Add the circle for this city to the map.
    new google.maps.Circle({
        strokeColor: color,
        strokeOpacity: 0.6,
        strokeWeight: 2,
        fillColor: color,
        fillOpacity: 0.4,
        map: map,
        center: marker.position,
        radius: mile * 1609.344
    });

    if (isCategoryA(category)) { // Category A
        new google.maps.Circle({
            strokeColor: '#FF0000',
            strokeOpacity: 0.6,
            strokeWeight: 2,
            fillColor: '#FF0000',
            fillOpacity: 0.2,
            map: map,
            center: marker.position,
            radius: 1.5 * 1609.344
        });
    }

    google.maps.event.addListener(marker, 'click', function () {
        if (opened_info) {
            opened_info.close();
        }
        opened_info = infowindow;
        infowindow.open(map, marker);
    });
}

function isNeedToShowItem(item , latlngBounds){

    if(item['show']){
        return false;
    }

    return latlngBounds.contains(new google.maps.LatLng(item[0], item[1]));
}

function checkNoFlyZone(pointList, pointCategory , latlngBounds) {

    pointList.forEach(function (item) {
        if (!isNeedToShowItem(item, latlngBounds)) {
            return;
        }
        item['show'] = true;
        show(map, item, pointCategory)
    });

}

function initializeNoWayPointZone(latlngBounds) {
    if(loadedLatLngBounds.contains(latlngBounds.getNorthEast())&&loadedLatLngBounds.contains(latlngBounds.getSouthWest())){
        alert(loadedLatLngBounds);
        return ;
    }
    loadedLatLngBounds = loadedLatLngBounds.union(latlngBounds);
    checkNoFlyZone(mc_category_city_limit, 'city_limit' , loadedLatLngBounds);
    checkNoFlyZone(mc_category_a, 'A',loadedLatLngBounds);
    checkNoFlyZone(mc_category_b, 'B',loadedLatLngBounds);

}