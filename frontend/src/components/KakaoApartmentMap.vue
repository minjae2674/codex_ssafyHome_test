<template>
  <section class="map-card">
    <div ref="mapElement" class="kakao-map"></div>

    <div class="map-floating-panel">
      <p class="map-floating-title">{{ regionLabel }}</p>
      <p class="map-floating-meta">{{ apartments.length.toLocaleString() }}개 단지</p>
    </div>

    <div v-if="mapError" class="map-key-warning">
      {{ mapError }}
    </div>
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
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
  regionLabel: {
    type: String,
    default: '대한민국',
  },
});

const emit = defineEmits(['selectApartment']);

const mapElement = ref(null);
const mapError = ref('');

let kakaoMap = null;
let kakaoSdk = null;
let apartmentMarkers = [];

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

function refreshMarkers() {
  if (!kakaoMap || !kakaoSdk) {
    return;
  }

  clearMarkers();
  apartmentMarkers = props.apartments
    .map(createMarker)
    .filter(Boolean);

  if (apartmentMarkers.length > 1) {
    const bounds = new kakaoSdk.maps.LatLngBounds();
    apartmentMarkers.forEach((marker) => bounds.extend(marker.getPosition()));
    kakaoMap.setBounds(bounds);
  }
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

onBeforeUnmount(() => {
  clearMarkers();
  kakaoMap = null;
});
</script>
