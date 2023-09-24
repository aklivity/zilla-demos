// This is an example file and is expected to be cloned
// without the -example on the same folder that it resides.

/**
 * The object below contains the default settings that are configurable via
 * app interface in src/fragments/forms/settings.vue. If the user changes a value for
 * a given setting option, then it will be saved in the localStorage, loaded on the app load
 * and will override the default value.  The options for each settings value are defined
 * in src/config/settings-options-example.js. You can change the default value, but not
 * remove defaultMapSettings props.
 */

const defaultMapSettings = {
  apiBaseUrl: 'https://api.openrouteservice.org',
  saveToLocalStorage: true,
  elevationProfile: true,
  apiKey: null,
  endpoints: null,
  locale: 'en-us',
  routingInstructionsLocale: 'en',
  unit: 'km',
  alwaysFitBounds: false,
  areaUnit: 'km',
  defaultTilesProvider: 'osm',
  customTileProviderUrl: '',
  customOverlayerTileProviderUrl: '',
  prioritizeSearchingForNearbyPlaces: true,
  defaultProfile: 'driving-car',
  compressDataUrlSegment: true,
  autoFitHighlightedBounds: true,
  convertStopAfterRouteEndingToDestination: false,
  useStopOptimization: false,
  accessibleModeActive: false,
  shownOnceTooltips: {},
  // mapCenter: {lat: 37.32897976943132, lng: -121.88891172409059}, // Convention Center, San Jose, CA, USA
  mapCenter: {lat: 37.334678404943276, lng: -121.88837528228761}, // downtown
  defaultIsochroneColors: [  // qgis plugin color scheme as default
    '#2b83ba',
    '#64abb0',
    '#9dd3a7',
    '#c7e9ad',
    '#edf8b9',
    '#ffedaa',
    '#fec980',
    '#f99e59',
    '#e85b3a',
    '#d7191c'
  ],
  alternativeIsochroneColors: false,
  distanceMarkers: false,
  skipAllSegments: false,

  steepness: true,
  surface: true,
  waytype: true,
  tollways: false,
  waycategory: false,
  traildifficulty: false,
  roadaccessrestrictions: false,
  green: false,
  noise: false,

  // Extra settings not being used yet
  suitability: false
}

export default defaultMapSettings
