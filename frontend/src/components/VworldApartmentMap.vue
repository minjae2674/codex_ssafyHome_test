<template>
  <section class="map-card">
    <div ref="mapElement" class="vworld-map"></div>

    <div class="map-floating-panel">
      <p class="map-floating-title">{{ regionLabel }}</p>
      <p class="map-floating-meta">{{ apartments.length.toLocaleString() }}개 단지</p>
    </div>

    <div v-if="!hasVworldKey" class="map-key-warning">
      VWorld 키가 없어서 임시 지도를 표시합니다.
    </div>
  </section>
</template>

<script setup>
import 'ol/ol.css';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import Map from 'ol/Map';
import View from 'ol/View';
import Feature from 'ol/Feature';
import Point from 'ol/geom/Point';
import TileLayer from 'ol/layer/Tile';
import VectorLayer from 'ol/layer/Vector';
import XYZ from 'ol/source/XYZ';
import VectorSource from 'ol/source/Vector';
import { Circle as CircleStyle, Fill, Stroke, Style } from 'ol/style';
import { fromLonLat } from 'ol/proj';
import { isEmpty } from 'ol/extent';

const props = defineProps({
  center: {
    type: Object,
    required: true,
  },
  apartments: {
    type: Array,
    default: () => [],
  },
  regionLabel: {
    type: String,
    default: '대한민국',
  },
});

const emit = defineEmits(['selectApartment']);

const mapElement = ref(null);
const hasVworldKey = computed(() => Boolean(import.meta.env.VITE_VWORLD_API_KEY));

let map = null;
let markerSource = null;

const markerStyle = new Style({
  image: new CircleStyle({
    radius: 7,
    fill: new Fill({ color: '#0b73e8' }),
    stroke: new Stroke({ color: '#ffffff', width: 2 }),
  }),
});

function toMapCenter(center) {
  const lng = Number(center.lng || 127.978);
  const lat = Number(center.lat || 36.5);
  return fromLonLat([lng, lat]);
}

function createTileLayer() {
  const key = import.meta.env.VITE_VWORLD_API_KEY;
  const url = key
    ? `https://api.vworld.kr/req/wmts/1.0.0/${key}/Base/{z}/{y}/{x}.png`
    : 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';

  return new TileLayer({
    source: new XYZ({
      url,
      crossOrigin: 'anonymous',
      maxZoom: 19,
    }),
  });
}

function createMarkerFeature(apartment) {
  const longitude = Number(apartment.longitude);
  const latitude = Number(apartment.latitude);

  if (!longitude || !latitude) {
    return null;
  }

  const feature = new Feature({
    geometry: new Point(fromLonLat([longitude, latitude])),
  });
  feature.set('apartment', apartment);
  feature.setStyle(markerStyle);
  return feature;
}

function refreshMarkers() {
  if (!markerSource) {
    return;
  }

  markerSource.clear();
  const features = props.apartments
    .map(createMarkerFeature)
    .filter(Boolean);
  markerSource.addFeatures(features);

  if (map && features.length > 1) {
    const extent = markerSource.getExtent();
    if (!isEmpty(extent)) {
      map.getView().fit(extent, {
        padding: [56, 56, 56, 56],
        duration: 350,
        maxZoom: 15,
      });
    }
  }
}

function moveToCenter(center) {
  if (!map) {
    return;
  }

  map.getView().animate({
    center: toMapCenter(center),
    zoom: center.zoom || 10,
    duration: 350,
  });
}

onMounted(() => {
  markerSource = new VectorSource();

  map = new Map({
    target: mapElement.value,
    layers: [
      createTileLayer(),
      new VectorLayer({ source: markerSource }),
    ],
    view: new View({
      center: toMapCenter(props.center),
      zoom: props.center.zoom || 7,
      minZoom: 6,
      maxZoom: 18,
    }),
  });

  map.on('click', (event) => {
    const feature = map.forEachFeatureAtPixel(event.pixel, (clickedFeature) => clickedFeature);
    if (feature) {
      emit('selectApartment', feature.get('apartment'));
    }
  });

  refreshMarkers();
});

watch(
  () => props.center,
  (nextCenter) => moveToCenter(nextCenter),
  { deep: true },
);

watch(
  () => props.apartments,
  () => refreshMarkers(),
  { deep: true },
);

onBeforeUnmount(() => {
  if (map) {
    map.setTarget(undefined);
    map = null;
  }
});
</script>
