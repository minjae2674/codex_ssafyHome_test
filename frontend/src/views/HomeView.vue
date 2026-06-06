<template>
  <main class="home-page">
    <aside class="region-panel">
      <section class="search-box">
        <div class="search-input-wrap">
          <Search :size="18" />
          <input
            v-model.trim="keyword"
            type="search"
            placeholder="아파트, 지역, 단지명 검색"
            @keyup.enter="handleKeywordSearch"
          />
        </div>
        <button class="icon-button primary" type="button" title="검색" @click="handleKeywordSearch">
          <Search :size="18" />
        </button>
      </section>

      <section class="region-step">
        <div class="section-title-row">
          <h2>시도</h2>
          <span>{{ sidos.length }}</span>
        </div>
        <div class="chip-grid">
          <button
            v-for="sido in sidos"
            :key="sido.code"
            type="button"
            class="region-chip"
            :class="{ active: selectedSido?.code === sido.code }"
            @click="selectSido(sido)"
          >
            <span>{{ shortRegionName(sido.name) }}</span>
            <small>{{ formatCount(sido.apartmentCount) }}</small>
          </button>
        </div>
      </section>

      <section class="region-step">
        <div class="section-title-row">
          <h2>시군구</h2>
          <span>{{ guguns.length }}</span>
        </div>
        <div v-if="guguns.length" class="chip-grid compact">
          <button
            v-for="gugun in guguns"
            :key="gugun.code"
            type="button"
            class="region-chip"
            :class="{ active: selectedGugun?.code === gugun.code }"
            @click="selectGugun(gugun)"
          >
            <span>{{ gugun.name }}</span>
            <small>{{ formatCount(gugun.apartmentCount) }}</small>
          </button>
        </div>
        <p v-else class="empty-copy">시도를 선택하세요.</p>
      </section>

      <section class="region-step">
        <div class="section-title-row">
          <h2>읍면동</h2>
          <span>{{ dongs.length }}</span>
        </div>
        <div v-if="dongs.length" class="chip-list">
          <button
            v-for="dong in dongs"
            :key="dong.code"
            type="button"
            class="dong-row"
            :class="{ active: selectedDong?.code === dong.code }"
            @click="selectDong(dong)"
          >
            <span>{{ dong.name }}</span>
            <strong>{{ formatCount(dong.apartmentCount) }}</strong>
          </button>
        </div>
        <p v-else class="empty-copy">시군구를 선택하세요.</p>
      </section>
    </aside>

    <section class="map-section">
      <div class="map-toolbar">
        <div>
          <p class="eyebrow">지도 탐색</p>
          <h1>{{ currentRegionLabel }}</h1>
        </div>
        <div class="mode-tabs" aria-label="지역 단계">
          <button type="button" :class="{ active: currentStep === 'sido' }">시도</button>
          <button type="button" :class="{ active: currentStep === 'gugun' }">시군구</button>
          <button type="button" :class="{ active: currentStep === 'dong' }">읍면동</button>
        </div>
      </div>

      <VworldApartmentMap
        :center="mapCenter"
        :apartments="apartments"
        :region-label="currentRegionLabel"
        @select-apartment="selectApartment"
      />
    </section>

    <aside class="apartment-panel">
      <section class="summary-card">
        <p class="eyebrow">검색 결과</p>
        <h2>{{ totalCount.toLocaleString() }}개 단지</h2>
        <p>{{ currentRegionLabel }}</p>
      </section>

      <section v-if="selectedApartment" class="selected-card">
        <p class="eyebrow">선택 단지</p>
        <h2>{{ selectedApartment.aptName }}</h2>
        <dl>
          <div>
            <dt>주소</dt>
            <dd>{{ selectedApartment.sidoName }} {{ selectedApartment.gugunName }} {{ selectedApartment.dongName }}</dd>
          </div>
          <div>
            <dt>준공</dt>
            <dd>{{ selectedApartment.buildYear || '-' }}</dd>
          </div>
          <div>
            <dt>지번</dt>
            <dd>{{ selectedApartment.jibun || '-' }}</dd>
          </div>
        </dl>
      </section>

      <section class="list-card">
        <div class="section-title-row">
          <h2>아파트</h2>
          <span>{{ apartments.length }}</span>
        </div>

        <div v-if="isLoading" class="loading-box">불러오는 중</div>

        <button
          v-for="apartment in apartments"
          :key="apartment.aptSeq"
          type="button"
          class="apartment-row"
          :class="{ active: selectedApartment?.aptSeq === apartment.aptSeq }"
          @click="selectApartment(apartment)"
        >
          <span class="apt-name">{{ apartment.aptName }}</span>
          <span class="apt-address">
            {{ apartment.gugunName }} {{ apartment.dongName }} · {{ apartment.buildYear || '-' }}
          </span>
        </button>

        <p v-if="!isLoading && !apartments.length" class="empty-copy">검색 결과가 없습니다.</p>
      </section>
    </aside>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { Search } from 'lucide-vue-next';
import { fetchDongs, fetchGuguns, fetchSidos } from '@/api/regionApi';
import { searchApartments } from '@/api/apartmentApi';
import VworldApartmentMap from '@/components/VworldApartmentMap.vue';

const DEFAULT_CENTER = { lat: 36.5, lng: 127.978, zoom: 7 };

const keyword = ref('');
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

const currentStep = computed(() => {
  if (selectedRegion.dong) return 'dong';
  if (selectedRegion.gugun) return 'gugun';
  return 'sido';
});

const currentRegionLabel = computed(() => {
  return [
    selectedRegion.sido?.name,
    selectedRegion.gugun?.name,
    selectedRegion.dong?.name,
  ].filter(Boolean).join(' ') || '전국';
});

function formatCount(count) {
  return `${Number(count || 0).toLocaleString()}개`;
}

function shortRegionName(name) {
  return name
    .replace('특별시', '')
    .replace('광역시', '')
    .replace('특별자치시', '')
    .replace('특별자치도', '')
    .replace('도', '');
}

function moveMapTo(region, zoom) {
  if (!region?.centerLat || !region?.centerLng) {
    return;
  }

  mapCenter.value = {
    lat: region.centerLat,
    lng: region.centerLng,
    zoom,
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
  dongs.value = [];
  guguns.value = await fetchGuguns(sido.name);
  moveMapTo(sido, 9);
  await loadApartments();
}

async function selectGugun(gugun) {
  selectedRegion.gugun = gugun;
  selectedRegion.dong = null;
  selectedApartment.value = null;
  dongs.value = await fetchDongs(selectedRegion.sido.name, gugun.name);
  moveMapTo(gugun, 12);
  await loadApartments();
}

async function selectDong(dong) {
  selectedRegion.dong = dong;
  selectedApartment.value = null;
  moveMapTo(dong, 15);
  await loadApartments();
}

async function handleKeywordSearch() {
  selectedApartment.value = null;
  await loadApartments();
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
    zoom: 16,
  };
}

onMounted(async () => {
  await loadSidos();
  await loadApartments();
});
</script>
