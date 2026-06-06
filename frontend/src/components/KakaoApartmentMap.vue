<template>
  <section class="map-card">
    <div ref="mapElement" class="kakao-map"></div>

    <div class="map-floating-panel">
      <p class="map-floating-title">{{ regionLabel }}</p>
      <p class="map-floating-meta">{{ floatingMetaLabel }}</p>
    </div>

    <div v-if="mapError" class="map-key-warning">
      {{ mapError }}
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { loadKakaoMapSdk } from '@/utils/kakaoMapLoader';

const props = defineProps({
  center: {
    type: Object,
    required: true,
  },
  apartments: {
    type: Array,
    default: () => [],
  },
  regions: {
    type: Array,
    default: () => [],
  },
  activeLevel: {
    type: String,
    default: 'sido',
  },
  showApartmentMarkers: {
    type: Boolean,
    default: false,
  },
  regionLabel: {
    type: String,
    default: '대한민국',
  },
});

const emit = defineEmits(['selectApartment', 'selectRegion']);

const mapElement = ref(null);
const mapError = ref('');

let kakaoMap = null;
let kakaoSdk = null;
let apartmentMarkers = [];
let regionOverlays = [];

const SIDO_DISPLAY_CENTERS = {
  11: { lat: 37.6700, lng: 126.9900 },
  26: { lat: 35.1796, lng: 129.0756 },
  27: { lat: 35.8714, lng: 128.6014 },
  28: { lat: 37.4300, lng: 126.6200 },
  29: { lat: 35.1595, lng: 126.8526 },
  30: { lat: 36.3504, lng: 127.3845 },
  31: { lat: 35.5384, lng: 129.3114 },
  36: { lat: 36.4800, lng: 127.2890 },
  41: { lat: 37.2600, lng: 127.3300 },
  42: { lat: 37.8228, lng: 128.1555 },
  43: { lat: 36.6357, lng: 127.4912 },
  44: { lat: 36.6588, lng: 126.6728 },
  45: { lat: 35.7175, lng: 127.1530 },
  46: { lat: 34.8679, lng: 126.9910 },
  47: { lat: 36.4919, lng: 128.8889 },
  48: { lat: 35.4606, lng: 128.2132 },
  50: { lat: 33.4996, lng: 126.5312 },
};

const floatingMetaLabel = computed(() => {
  if (props.showApartmentMarkers) {
    return `${props.apartments.length.toLocaleString()}개 단지`;
  }

  return `${props.regions.length.toLocaleString()}개 지역`;
});

function toLatLng(center) {
  const latitude = Number(center.lat || 36.5);
  const longitude = Number(center.lng || 127.978);
  return new kakaoSdk.maps.LatLng(latitude, longitude);
}

function toMarkerLatLng(apartment) {
  const latitude = Number(apartment.latitude);
  const longitude = Number(apartment.longitude);

  if (!latitude || !longitude) {
    return null;
  }

  return new kakaoSdk.maps.LatLng(latitude, longitude);
}

function getMapLevel(center) {
  return Number(center.level || 8);
}

function clearMarkers() {
  apartmentMarkers.forEach((marker) => marker.setMap(null));
  apartmentMarkers = [];
}

function clearRegionOverlays() {
  regionOverlays.forEach((overlay) => overlay.setMap(null));
  regionOverlays = [];
}

function getRegionLabel(region) {
  if (props.activeLevel === 'sido') {
    return region.name
      .replace('특별시', '')
      .replace('광역시', '')
      .replace('특별자치시', '')
      .replace('특별자치도', '')
      .replace('도', '');
  }

  return region.name;
}

function toRegionLatLng(region) {
  const displayCenter = props.activeLevel === 'sido'
    ? SIDO_DISPLAY_CENTERS[region.code]
    : null;
  const latitude = Number(displayCenter?.lat || region.centerLat);
  const longitude = Number(displayCenter?.lng || region.centerLng);

  if (!latitude || !longitude) {
    return null;
  }

  return new kakaoSdk.maps.LatLng(latitude, longitude);
}

function createMarker(apartment) {
  const markerPosition = toMarkerLatLng(apartment);

  if (!markerPosition) {
    return null;
  }

  const marker = new kakaoSdk.maps.Marker({
    map: kakaoMap,
    position: markerPosition,
    title: apartment.aptName,
    clickable: true,
  });

  kakaoSdk.maps.event.addListener(marker, 'click', () => {
    emit('selectApartment', apartment);
  });

  return marker;
}

function createRegionOverlay(region) {
  const overlayPosition = toRegionLatLng(region);

  if (!overlayPosition) {
    return null;
  }

  const overlayButton = document.createElement('button');
  overlayButton.type = 'button';
  overlayButton.className = `region-map-overlay region-map-overlay--${props.activeLevel}`;
  overlayButton.setAttribute('aria-label', `${region.name} 지역으로 이동`);
  overlayButton.innerHTML = `
    <span class="region-map-overlay__name">${getRegionLabel(region)}</span>
    <span class="region-map-overlay__count">${Number(region.apartmentCount || 0).toLocaleString()}개 단지</span>
  `;
  overlayButton.addEventListener('click', () => {
    emit('selectRegion', region);
  });

  return new kakaoSdk.maps.CustomOverlay({
    map: kakaoMap,
    position: overlayPosition,
    content: overlayButton,
    yAnchor: 1.15,
    zIndex: 1000000 - Number(region.code || 0),
  });
}

function refreshMarkers() {
  if (!kakaoMap || !kakaoSdk) {
    return;
  }

  clearMarkers();

  if (!props.showApartmentMarkers) {
    return;
  }

  apartmentMarkers = props.apartments
    .map(createMarker)
    .filter(Boolean);

  if (apartmentMarkers.length > 1) {
    const bounds = new kakaoSdk.maps.LatLngBounds();
    apartmentMarkers.forEach((marker) => bounds.extend(marker.getPosition()));
    kakaoMap.setBounds(bounds);
  }
}

function refreshRegionOverlays() {
  if (!kakaoMap || !kakaoSdk) {
    return;
  }

  clearRegionOverlays();

  if (props.showApartmentMarkers) {
    return;
  }

  regionOverlays = props.regions
    .map(createRegionOverlay)
    .filter(Boolean);
}

function moveToCenter(center) {
  if (!kakaoMap || !kakaoSdk) {
    return;
  }

  kakaoMap.setCenter(toLatLng(center));
  kakaoMap.setLevel(getMapLevel(center));
}

async function initializeMap() {
  try {
    kakaoSdk = await loadKakaoMapSdk();
    kakaoMap = new kakaoSdk.maps.Map(mapElement.value, {
      center: toLatLng(props.center),
      level: getMapLevel(props.center),
    });

    refreshRegionOverlays();
    refreshMarkers();
  } catch (error) {
    mapError.value = error.message;
  }
}

onMounted(() => {
  initializeMap();
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

watch(
  () => [props.regions, props.activeLevel, props.showApartmentMarkers],
  () => {
    refreshRegionOverlays();
    refreshMarkers();
  },
  { deep: true },
);

onBeforeUnmount(() => {
  clearMarkers();
  clearRegionOverlays();
  kakaoMap = null;
});
</script>
