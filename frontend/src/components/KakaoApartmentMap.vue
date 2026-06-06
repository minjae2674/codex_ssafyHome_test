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
  selectedAptSeq: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['selectApartment', 'selectRegion']);

const mapElement = ref(null);
const mapError = ref('');

let kakaoMap = null;
let kakaoSdk = null;
let apartmentOverlays = [];
let regionOverlays = [];

const SIDO_DISPLAY_CENTERS = {
  11: { lat: 37.7200, lng: 126.9500 },
  26: { lat: 35.1200, lng: 129.1700 },
  27: { lat: 35.7700, lng: 128.6500 },
  28: { lat: 37.3300, lng: 126.4300 },
  29: { lat: 35.1595, lng: 126.8526 },
  30: { lat: 36.2100, lng: 127.3300 },
  31: { lat: 35.6200, lng: 129.3000 },
  36: { lat: 36.6100, lng: 127.0800 },
  41: { lat: 37.3800, lng: 127.6500 },
  42: { lat: 37.8228, lng: 128.1555 },
  43: { lat: 36.8200, lng: 127.7400 },
  44: { lat: 36.3200, lng: 126.6000 },
  45: { lat: 35.6200, lng: 127.1700 },
  46: { lat: 34.9000, lng: 126.6800 },
  47: { lat: 36.4100, lng: 128.8000 },
  48: { lat: 35.1800, lng: 128.1700 },
  50: { lat: 33.4996, lng: 126.5312 },
};

const SIDO_SHORT_NAMES = {
  11: '서울',
  26: '부산',
  27: '대구',
  28: '인천',
  29: '광주',
  30: '대전',
  31: '울산',
  36: '세종',
  41: '경기',
  42: '강원',
  43: '충북',
  44: '충남',
  45: '전북',
  46: '전남',
  47: '경북',
  48: '경남',
  50: '제주',
};

const MANY_APARTMENT_COUNT = 40;
const MEDIUM_APARTMENT_COUNT = 15;

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
  apartmentOverlays.forEach(({ overlay }) => overlay.setMap(null));
  apartmentOverlays = [];
}

function clearRegionOverlays() {
  regionOverlays.forEach((overlay) => overlay.setMap(null));
  regionOverlays = [];
}

function getRegionLabel(region) {
  if (props.activeLevel === 'sido') {
    if (SIDO_SHORT_NAMES[region.code]) {
      return SIDO_SHORT_NAMES[region.code];
    }

    return region.name
      .replace('특별시', '')
      .replace('광역시', '')
      .replace('특별자치시', '')
      .replace('특별자치도', '')
      .replace('도', '');
  }

  return region.name;
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString();
}

function getApartmentMeta(apartment) {
  const buildYear = apartment.buildYear ? `${apartment.buildYear}년` : null;
  const householdCount = apartment.householdCount || apartment.households || apartment.household;
  const householdLabel = householdCount ? `${formatNumber(householdCount)}세대` : null;
  const locationLabel = apartment.dongName || apartment.gugunName || null;

  return [buildYear, householdLabel || locationLabel].filter(Boolean).join(' | ') || '단지 정보';
}

function getMedian(values) {
  const sortedValues = [...values].sort((left, right) => left - right);
  const middleIndex = Math.floor(sortedValues.length / 2);

  if (sortedValues.length % 2) {
    return sortedValues[middleIndex];
  }

  return (sortedValues[middleIndex - 1] + sortedValues[middleIndex]) / 2;
}

function getMedianPosition(positions) {
  const latitudes = positions.map((position) => position.getLat());
  const longitudes = positions.map((position) => position.getLng());

  return {
    lat: getMedian(latitudes),
    lng: getMedian(longitudes),
  };
}

function getDistanceFromCenter(position, center) {
  const latDistance = (position.getLat() - center.lat) * 111;
  const lngDistance = (position.getLng() - center.lng) * 88;

  return Math.sqrt(latDistance ** 2 + lngDistance ** 2);
}

function getPositionsWithoutOutliers(positions) {
  if (positions.length < 10) {
    return positions;
  }

  const center = getMedianPosition(positions);
  const positionsByDistance = positions
    .map((position) => ({
      position,
      distance: getDistanceFromCenter(position, center),
    }))
    .sort((left, right) => left.distance - right.distance);

  const keepCount = Math.max(1, Math.ceil(positions.length * 0.85));
  return positionsByDistance.slice(0, keepCount).map((item) => item.position);
}

function fitMapToApartmentOverlays() {
  if (apartmentOverlays.length <= 1 || props.selectedAptSeq) {
    return;
  }

  const positions = apartmentOverlays.map(({ position }) => position);

  // 단지가 많은 동은 모든 좌표를 한 화면에 맞추면 카드가 겹치고, 좌표 이상치 하나에도 과하게 줌아웃된다.
  // 그래서 중앙값 좌표를 기준으로 확대해서 사용자가 지도 위 단지명을 읽을 수 있게 한다.
  if (positions.length >= MANY_APARTMENT_COUNT) {
    const center = getMedianPosition(positions);

    kakaoMap.setCenter(new kakaoSdk.maps.LatLng(center.lat, center.lng));
    kakaoMap.setLevel(4);
    return;
  }

  const bounds = new kakaoSdk.maps.LatLngBounds();
  const visiblePositions = positions.length >= MEDIUM_APARTMENT_COUNT
    ? getPositionsWithoutOutliers(positions)
    : positions;

  visiblePositions.forEach((position) => bounds.extend(position));
  kakaoMap.setBounds(bounds);

  if (
    positions.length >= MEDIUM_APARTMENT_COUNT
    && typeof kakaoMap.getLevel === 'function'
    && kakaoMap.getLevel() > 4
  ) {
    kakaoMap.setLevel(4);
  }
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

function createApartmentOverlay(apartment) {
  const overlayPosition = toMarkerLatLng(apartment);

  if (!overlayPosition) {
    return null;
  }

  const overlayButton = document.createElement('button');
  overlayButton.type = 'button';
  overlayButton.className = 'apartment-map-overlay';
  overlayButton.setAttribute('aria-label', `${apartment.aptName} 단지 상세 보기`);

  if (props.selectedAptSeq === apartment.aptSeq) {
    overlayButton.classList.add('apartment-map-overlay--active');
  }

  const icon = document.createElement('span');
  icon.className = 'apartment-map-overlay__icon';
  icon.setAttribute('aria-hidden', 'true');

  const textBox = document.createElement('span');
  textBox.className = 'apartment-map-overlay__text';

  const name = document.createElement('span');
  name.className = 'apartment-map-overlay__name';
  name.textContent = apartment.aptName;

  const meta = document.createElement('span');
  meta.className = 'apartment-map-overlay__meta';
  meta.textContent = getApartmentMeta(apartment);

  textBox.append(name, meta);
  overlayButton.append(icon, textBox);

  overlayButton.addEventListener('click', (event) => {
    event.preventDefault();
    event.stopPropagation();
    emit('selectApartment', apartment);
  });

  const overlay = new kakaoSdk.maps.CustomOverlay({
    map: kakaoMap,
    position: overlayPosition,
    content: overlayButton,
    yAnchor: 1.35,
    zIndex: props.selectedAptSeq === apartment.aptSeq ? 3000000 : 2000000,
  });

  return { overlay, position: overlayPosition };
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

  const name = document.createElement('span');
  name.className = 'region-map-overlay__name';
  name.textContent = getRegionLabel(region);

  const count = document.createElement('span');
  count.className = 'region-map-overlay__count';
  count.textContent = formatNumber(region.apartmentCount);

  overlayButton.append(name, count);
  overlayButton.addEventListener('click', (event) => {
    event.preventDefault();
    event.stopPropagation();
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

  apartmentOverlays = props.apartments
    .map(createApartmentOverlay)
    .filter(Boolean);

  fitMapToApartmentOverlays();
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
  () => [props.regions, props.activeLevel, props.showApartmentMarkers, props.selectedAptSeq],
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
