/**
 * This is an example of a simple plugin aiming at demonstrating how you can add
 * custom behavior to the maps-client. To make the plugin work it is necessary
 * to import this class in the hooks.js, instantiate it and run the plugin methods
 * on hooks defined on hooks.js
 * @see /src/config/hook-example.js
 *
 * It is possible to use values from the store, like, for example:
 * store.getters.mapCenter, store.getters.mapBounds and store.getters.mode
 * It is also possible to emit events using the EventBus. For example:
 * EventBus.$emit('mapViewDataChanged', mapViewDataChanged)
 */
import Vue from 'vue'
import {VBtn, VInput} from 'vuetify'
import Leaflet from 'leaflet'
import {EventBus} from '@/common/event-bus'
import Place from '@/models/place'
import MapViewData from '@/models/map-view-data'

const barData = [
  {
    'location': {
      'x': -121.886548,
      'y': 37.329916
    },
    'name': 'Haberdasher'
  },
  {
    'location': {
      'x': -121.886489,
      'y': 37.33015
    },
    'name': 'The Fountainhead Bar'
  },
  {
    'location': {
      'x': -121.886583,
      'y': 37.33057
    },
    'name': 'The Continental Bar Lounge & Patio'
  },
  {
    'location': {
      'x': -121.8858,
      'y': 37.329313
    },
    'name': 'Uproar Brewing Company'
  },
  {
    'location': {
      'x': -121.892301,
      'y': 37.333053
    },
    'name': 'Caravan Lounge'
  },
  {
    'location': {
      'x': -121.88997,
      'y': 37.334991
    },
    'name': 'Skewers & Brew'
  },
  {
    'location': {
      'x': -121.889403,
      'y': 37.335097
    },
    'name': 'Paper Plane'
  },
  {
    'location': {
      'x': -121.890903,
      'y': 37.335023
    },
    'name': 'Splash Video Dance Bar'
  },
  {
    'location': {
      'x': -121.890482,
      'y': 37.335185
    },
    'name': 'Mac\'s Club'
  },
  {
    'location': {
      'x': -121.89362,
      'y': 37.334962
    },
    'name': 'Five Points'
  },
  {
    'location': {
      'x': -121.889894,
      'y': 37.336244
    },
    'name': 'Fox Tale Fermentation Project'
  },
  {
    'location': {
      'x': -121.893356,
      'y': 37.335375
    },
    'name': 'Olla'
  },
  {
    'location': {
      'x': -121.893436,
      'y': 37.335533
    },
    'name': 'O\'Flaherty\'s Irish Pub'
  },
  {
    'location': {
      'x': -121.889293,
      'y': 37.337041
    },
    'name': 'ISO Beers'
  },
  {
    'location': {
      'x': -121.894111,
      'y': 37.336109
    },
    'name': 'District San Jose'
  },
  {
    'location': {
      'x': -121.888879,
      'y': 37.337333
    },
    'name': '3rd & Bourbon'
  },
  {
    'location': {
      'x': -121.894348,
      'y': 37.336519
    },
    'name': 'San Pedro Square Market'
  },
  {
    'location': {
      'x': -121.894278,
      'y': 37.336556
    },
    'name': 'San Pedro Square Market Bar'
  },
  {
    'location': {
      'x': -121.897993,
      'y': 37.335078
    },
    'name': 'Enoteca La Storia'
  },
  {
    'location': {
      'x': -121.90081,
      'y': 37.332876
    },
    'name': 'BMW Lounge'
  },
  {
    'location': {
      'x': -121.893671,
      'y': 37.339914
    },
    'name': 'Teske\'s Germania'
  },
  {
    'location': {
      'x': -121.892833,
      'y': 37.340302
    },
    'name': 'Devine Cheese And Wine'
  },
  {
    'location': {
      'x': -121.905419,
      'y': 37.332056
    },
    'name': 'True Brew San Jose'
  }
]

const Button = Vue.extend(VBtn)
const Input = Vue.extend(VInput)
const yesterday = new Date(new Date().setDate(new Date().getDate()-1)).valueOf()

class VehicleLocationPlugin {
  /**
   * PluginExample constructor.
   * IMPORTANT: this constructor is expected
   * to be called on the hooks.js the `appLoaded` hook
   */
  constructor (vueInstance) {
    this.vueInstance = vueInstance
    this.taxiRouteAPI = 'http://localhost:8085/taxiroute.TaxiRoute/CreateTaxi'
    this.taxiLocationAPI = 'http://localhost:7114/taxi/locations'
    this.busLocationAPI = 'http://localhost:7114/bus/locations'
    this.timer = null
    this.localMapViewData = new MapViewData()
    this.routeKeyPath = ''
    this.barPlaces = barData.map((b) => (new Place(b.location.x, b.location.y, b.name, { isPoi: true })))
  }

  appLoaded(vueInstance) {
    console.log('VehicleLocationPlugin: appLoaded callback', vueInstance)

    this.timer = setInterval(async () => {
      if (this.localMapViewData.routes.length == 0 || (this.routeKeyPath != '' && this.localMapViewData.routes.length > 0)){
        var res = await fetch(this.taxiLocationAPI + this.routeKeyPath)
        if (res.status == 200) {
          var locations = await res.json()
          var mapData = this.localMapViewData
          if (mapData) {
            if (Array.isArray(locations)) {
              mapData.places = []
              if(mapData.pois.length == 0) mapData.pois = this.barPlaces

              var busses = await fetch(this.busLocationAPI).then((r) => r.json())
              locations = [...locations, ...busses]
              locations.forEach(({key, coordinate, icon}) => {
                if (coordinate[coordinate.length - 1] != -1) {
                  mapData.places.push(new Place(coordinate[0], coordinate[1], key, { icon }))
                }
              })
            } else {
              mapData.pois = []
              var coordinate = locations.coordinate
              if (coordinate[coordinate.length - 1] != -1 && mapData.places.length >= 2) {
                mapData.places = [
                  mapData.places[0],
                  new Place(coordinate[0], coordinate[1], locations.key),
                  mapData.places[mapData.places.length -1]
                ]
              } else {
                mapData.places = [
                  mapData.places[0],
                  mapData.places[mapData.places.length -1]
                ]
                this.routeKeyPath = ''
              }
            }
            EventBus.$emit('mapViewDataChanged', mapData)
          }
        }
      }
    }, 2000)
  }

  mapViewDataChanged (mapViewData) {
    this.localMapViewData = mapViewData.clone()
    if (!mapViewData.routes.length) {
      this.routeKeyPath = ''
    }
  }

  async afterBuildDirectionsMapViewData (mapViewData) {
    if (mapViewData.routes.length) {
      var route = mapViewData.routes[0]
      var safeName = encodeURIComponent(mapViewData.places[mapViewData.places.length - 1].placeName).replaceAll('%20','_').substring(0,13)
      var taxiRoute = {
        key: `${safeName}-${mapViewData.timestamp - yesterday}`,
        bbox: route.bbox,
        distance: route.summary.distance,
        duration: route.summary.duration,
        coordinates: route.geometry.coordinates,
      }
      var res = await fetch(this.taxiRouteAPI, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': `${taxiRoute.key}`,
        },
        body: JSON.stringify(taxiRoute),
      })
      if (res.status == 200) {
        this.routeKeyPath = `/${taxiRoute.key}`
      }
    } else {
      this.routeKeyPath = ''
    }
  }

  beforeOpenMarkerPopup({ markerPopupContainerRef, marker }) {

    // If the VueJS component was not already added
    let buttons = markerPopupContainerRef.querySelectorAll('a')
    if (buttons.length == 0 && marker.place.isPoi) {
      markerPopupContainerRef.innerText = marker.label

      //capture name doesn't work but formats the popup better
      var name = ''
      var input = new Input({ propsData: { value: name } })
      // input.$slots.label = ['Name']
      input.$on('input', (e) => name = e)
      input.$mount()
      markerPopupContainerRef.appendChild(input.$el)

      var btn = new Button({ propsData: { color: 'primary' } })
      btn.$slots.default = ['Hail a Taxi']
      btn.$on(['click'], () => {
        EventBus.$emit('setInputPlace', {
          pickPlaceIndex: 0,
          place: new Place(
            -121.888771,
            37.329079,
            'San Jose McEnery Convention Center',
          )
        })
        EventBus.$emit('directionsToPoint', {
          place: marker.place
        })
      })
      btn.$mount()
      markerPopupContainerRef.appendChild(btn.$el)
    }
  }

  poisMarkersCreated(markers) {
    let markerIcoStyle = 'transform: rotate(+45deg); margin-left: 4px;margin-top: 2px;'

    for (let key in markers) {
      let markerColor = 'red'
      let markerIcon = 'local_bar'
      let iconDivMarkerStyle = `color: white; width: 30px; height: 30px;border-radius: 50% 50% 50% 0;background: ${markerColor};position: absolute;transform: rotate(-45deg);left: 50%;top: 50%;margin: -15px 0 0 -15px;`
      markers[key].icon = Leaflet.divIcon({
        className: 'custom-div-icon',
        html: `<div style='${iconDivMarkerStyle}'><i style='${markerIcoStyle}' class='material-icons'>${markerIcon}</i></div>`,
        iconSize: [30, 42],
        iconAnchor: [15, 42],
      })
    }
    return markers
  }

  markersCreated(markers) {
    // Code example for customizing icons using material design icons https://material.io/resources/icons/
    let markerIcoStyle =
      'transform: rotate(+45deg); margin-left: 4px;margin-top: 2px;'
    for (let key in markers) {
      let markerColor = '#0d9b76' // maybe change the color based on the place properties ?
      let markerIcon = 'local_taxi' // maybe change the icon based on the place properties ?

      if (this.localMapViewData.routes.length > 0 ) {
        if (key == 0) {
          markerColor = 'green'
          markerIcon = 'location_on' // ma
        } else if (key == markers.length - 1) {
          markerColor = 'red'
          markerIcon = 'local_bar' // ma
        }

      }

      let iconDivMarkerStyle = `color: white; width: 30px; height: 30px;border-radius: 50% 50% 50% 0;background: ${markerColor};position: absolute;transform: rotate(-45deg);left: 50%;top: 50%;margin: -15px 0 0 -15px;`
      markers[key].icon = Leaflet.divIcon({
        className: 'custom-div-icon',
        html: `<div style='${iconDivMarkerStyle}'><i style='${markerIcoStyle}' class='material-icons'>${markers[key].place.icon || markerIcon}</i></div>`,
        iconSize: [30, 42],
        iconAnchor: [15, 42],
      })
    }
    return markers
  }

}
// export the AppHooks json builder class
export default VehicleLocationPlugin
