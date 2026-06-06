<template>
  <main class="home-page">
    <aside class="left-panel">
      <section class="region-selector">
        <div class="region-breadcrumb">
          <button
            type="button"
            :class="{ active: regionStep === 'sido' }"
            @click="resetToSido"
          >
            {{ selectedSido ? shortRegionName(selectedSido.name) : '시도 선택' }}
          </button>
          <ChevronRight :size="14" />
          <button
            type="button"
            :disabled="!selectedSido"
            :class="{ active: regionStep === 'gugun' }"
            @click="resetToGugun"
          >
            {{ selectedGugun?.name || '시군구 선택' }}
          </button>
          <ChevronRight :size="14" />
          <button
            type="button"
            :disabled="!selectedGugun"
            :class="{ active: regionStep === 'dong' }"
            @click="resetToDong"
          >
            {{ selectedDong?.name || '읍면동 선택' }}
          </button>
        </div>

        <div v-if="regionStep === 'sido'" class="region-selector-grid">
          <button
            v-for="sido in sidos"
            :key="sido.code"
            type="button"
            class="region-option"
            :class="{ active: selectedSido?.code === sido.code }"
            @click="selectSido(sido)"
          >
            <span>{{ shortRegionName(sido.name) }}</span>
            <small>{{ formatCount(sido.apartmentCount) }}</small>
          </button>
        </div>

        <div v-else-if="regionStep === 'gugun'" class="region-selector-grid">
          <button
            v-for="gugun in guguns"
            :key="gugun.code"
            type="button"
            class="region-option"
            :class="{ active: selectedGugun?.code === gugun.code }"
            @click="selectGugun(gugun)"
          >
            <span>{{ gugun.name }}</span>
            <small>{{ formatCount(gugun.apartmentCount) }}</small>
          </button>
          <p v-if="!guguns.length" class="region-empty">시군구 데이터가 없습니다.</p>
        </div>

        <div v-else-if="showDongSelector" class="region-selector-grid">
          <button
            v-for="dong in dongs"
            :key="dong.code"
            type="button"
            class="region-option"
            :class="{ active: selectedDong?.code === dong.code }"
            @click="selectDong(dong)"
          >
            <span>{{ dong.name }}</span>
            <small>{{ formatCount(dong.apartmentCount) }}</small>
          </button>
          <p v-if="!dongs.length" class="region-empty">읍면동 데이터가 없습니다.</p>
        </div>
      </section>

      <template v-if="hasSelectedDong">
        <section class="deal-filter">
          <button
            v-for="dealType in dealTypes"
            :key="dealType"
            type="button"
            :class="{ active: activeDealType === dealType }"
            @click="activeDealType = dealType"
          >
            {{ dealType }}
          </button>
        </section>

        <section class="quick-filter">
          <div class="sort-row">
            <button type="button" class="active">이름순</button>
            <span>|</span>
            <button type="button">거래량순</button>
            <span>|</span>
            <button type="button">입주년도순</button>
            <span>|</span>
            <button type="button">가격순</button>
          </div>
          <div class="property-legend">
            <span><i class="legend-box primary"></i>아파트</span>
            <span><i class="legend-box lease"></i>오피스텔</span>
            <span><i class="legend-box neutral"></i>연립다세대</span>
          </div>
        </section>
      </template>

      <section class="listing-panel">
        <div v-if="!hasSelectedDong" class="awaiting-region">
          <p>읍면동까지 선택해주세요</p>
          <span>시도 › 시군구 › 읍면동 순으로 선택</span>
        </div>

        <template v-else>
          <div class="result-header">
            <span>
              아파트 · {{ activeDealType }}
              <strong>{{ totalCount.toLocaleString() }}</strong>건
            </span>
            <small>{{ currentRegionLabel }}</small>
          </div>

          <div v-if="selectedApartment" class="selected-apartment-box">
            <p>선택 단지</p>
            <h2>{{ selectedApartment.aptName }}</h2>
            <span>
              {{ selectedApartment.sidoName }} {{ selectedApartment.gugunName }}
              {{ selectedApartment.dongName }} · {{ selectedApartment.buildYear || '-' }}년
            </span>
          </div>

          <div v-if="isLoading" class="loading-box">실거래가 불러오는 중...</div>

          <button
            v-for="apartment in apartments"
            :key="apartment.aptSeq"
            type="button"
            class="listing-card"
            :class="{ active: selectedApartment?.aptSeq === apartment.aptSeq }"
            @click="selectApartment(apartment)"
          >
            <span class="listing-title">{{ apartment.aptName }}</span>
            <span class="listing-meta">
              {{ apartment.gugunName }} {{ apartment.dongName }} · 준공 {{ apartment.buildYear || '-' }}
            </span>
            <span class="listing-address">지번 {{ apartment.jibun || '-' }}</span>
            <Heart :size="21" class="listing-heart" />
          </button>

          <p v-if="!isLoading && !apartments.length" class="empty-copy">
            조건에 맞는 아파트가 없습니다.
          </p>
        </template>
      </section>
    </aside>

    <section class="map-workspace">
      <div class="map-level-switcher" aria-label="지도 지역 단계">
        <button type="button" title="내 위치" @click="moveToMyLocation">
          <LocateFixed :size="16" />
        </button>
        <span></span>
        <button
          type="button"
          :class="{ active: activeLevel === 'sido' }"
          @click="changeMapLevel('sido')"
        >
          시도
        </button>
        <span></span>
        <button
          type="button"
          :class="{ active: activeLevel === 'gugun' }"
          @click="changeMapLevel('gugun')"
        >
          시군구
        </button>
        <span></span>
        <button
          type="button"
          :class="{ active: activeLevel === 'dong' }"
          @click="changeMapLevel('dong')"
        >
          읍면동
        </button>
      </div>

      <KakaoApartmentMap
        :center="mapCenter"
        :apartments="apartments"
        :region-label="currentRegionLabel"
        @select-apartment="selectApartment"
      />
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ChevronRight, Heart, LocateFixed } from 'lucide-vue-next';
import { fetchDongs, fetchGuguns, fetchSidos } from '@/api/regionApi';
import { searchApartments } from '@/api/apartmentApi';
import KakaoApartmentMap from '@/components/KakaoApartmentMap.vue';

const DEFAULT_CENTER = { lat: 36.5, lng: 127.978, level: 13 };
const dealTypes = ['매매', '전세', '월세'];

const route = useRoute();
const keyword = ref('');
const regionStep = ref('sido');
const dongListOpen = ref(true);
const activeDealType = ref('매매');
const sidos = ref([]);
const guguns = ref([]);
const dongs = ref([]);
const apartments = ref([]);
const totalCount = ref(0);
const isLoading = ref(false);
const selectedApartment = ref(null);
const mapCenter = ref(DEFAULT_CENTER);

const selectedRegion = reactive({
  sido: null,
  gugun: null,
  dong: null,
});

const selectedSido = computed(() => selectedRegion.sido);
const selectedGugun = computed(() => selectedRegion.gugun);
const selectedDong = computed(() => selectedRegion.dong);
const hasSelectedDong = computed(() => Boolean(selectedRegion.dong));

const showDongSelector = computed(() => {
  return regionStep.value === 'dong' && (!selectedRegion.dong || dongListOpen.value);
});

const activeLevel = computed(() => regionStep.value);

const currentRegionLabel = computed(() => {
  return [
    selectedRegion.sido?.name,
    selectedRegion.gugun?.name,
    selectedRegion.dong?.name,
  ].filter(Boolean).join(' ') || '전국';
});

function formatCount(count) {
  return `${Number(count || 0).toLocaleString()}`;
}

function shortRegionName(name) {
  return name
    .replace('특별시', '')
    .replace('광역시', '')
    .replace('특별자치시', '')
    .replace('특별자치도', '')
    .replace('도', '');
}

function moveMapTo(region, level) {
  if (!region?.centerLat || !region?.centerLng) {
    return;
  }

  mapCenter.value = {
    lat: region.centerLat,
    lng: region.centerLng,
    level,
  };
}

async function loadSidos() {
  sidos.value = await fetchSidos();
}

async function selectSido(sido) {
  selectedRegion.sido = sido;
  selectedRegion.gugun = null;
  selectedRegion.dong = null;
  selectedApartment.value = null;
  regionStep.value = 'gugun';
  dongListOpen.value = true;
  dongs.value = [];
  guguns.value = await fetchGuguns(sido.name);
  moveMapTo(sido, 9);
  await loadApartments();
}

async function selectGugun(gugun) {
  selectedRegion.gugun = gugun;
  selectedRegion.dong = null;
  selectedApartment.value = null;
  regionStep.value = 'dong';
  dongListOpen.value = true;
  dongs.value = await fetchDongs(selectedRegion.sido.name, gugun.name);
  moveMapTo(gugun, 6);
  await loadApartments();
}

async function selectDong(dong) {
  selectedRegion.dong = dong;
  selectedApartment.value = null;
  regionStep.value = 'dong';
  dongListOpen.value = false;
  moveMapTo(dong, 4);
  await loadApartments();
}

async function resetToSido() {
  selectedRegion.sido = null;
  selectedRegion.gugun = null;
  selectedRegion.dong = null;
  selectedApartment.value = null;
  regionStep.value = 'sido';
  dongListOpen.value = true;
  guguns.value = [];
  dongs.value = [];
  mapCenter.value = DEFAULT_CENTER;
  await loadApartments();
}

async function resetToGugun() {
  if (!selectedRegion.sido) {
    return;
  }

  selectedRegion.gugun = null;
  selectedRegion.dong = null;
  selectedApartment.value = null;
  regionStep.value = 'gugun';
  dongListOpen.value = true;
  dongs.value = [];
  moveMapTo(selectedRegion.sido, 9);
  await loadApartments();
}

async function resetToDong() {
  if (!selectedRegion.gugun) {
    return;
  }

  selectedRegion.dong = null;
  selectedApartment.value = null;
  regionStep.value = 'dong';
  dongListOpen.value = true;
  moveMapTo(selectedRegion.gugun, 6);
  await loadApartments();
}

function changeMapLevel(level) {
  if (level === 'sido') {
    resetToSido();
    return;
  }

  if (level === 'gugun') {
    if (selectedRegion.sido) {
      resetToGugun();
      return;
    }
    regionStep.value = 'sido';
    return;
  }

  if (selectedRegion.gugun) {
    resetToDong();
    return;
  }

  regionStep.value = selectedRegion.sido ? 'gugun' : 'sido';
}

function moveToMyLocation() {
  if (!navigator.geolocation) {
    return;
  }

  navigator.geolocation.getCurrentPosition((position) => {
    mapCenter.value = {
      lat: position.coords.latitude,
      lng: position.coords.longitude,
      level: 4,
    };
  });
}

async function loadApartments() {
  isLoading.value = true;

  try {
    const result = await searchApartments({
      keyword: keyword.value || undefined,
      sidoName: selectedRegion.sido?.name,
      gugunName: selectedRegion.gugun?.name,
      dongCode: selectedRegion.dong?.code,
      page: 1,
      size: 80,
    });

    apartments.value = result.apartments;
    totalCount.value = result.totalCount;
  } finally {
    isLoading.value = false;
  }
}

function selectApartment(apartment) {
  selectedApartment.value = apartment;
  mapCenter.value = {
    lat: apartment.latitude,
    lng: apartment.longitude,
    level: 3,
  };
}

watch(
  () => route.query.keyword,
  async (nextKeyword) => {
    keyword.value = typeof nextKeyword === 'string' ? nextKeyword : '';
    selectedApartment.value = null;
    await loadApartments();
  },
);

onMounted(async () => {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : '';
  await loadSidos();
  await loadApartments();
});
</script>
