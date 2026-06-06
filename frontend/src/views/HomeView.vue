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
        :regions="mapRegionOptions"
        :active-level="regionStep"
        :show-apartment-markers="hasSelectedDong"
        :region-label="currentRegionLabel"
        :selected-apt-seq="selectedApartment?.aptSeq || ''"
        @select-apartment="selectApartment"
        @select-region="selectMapRegion"
      />

      <aside v-if="selectedApartment" class="detail-panel">
        <header class="detail-header">
          <div>
            <p>단지 상세</p>
            <h2>{{ detailApartment.aptName }}</h2>
          </div>
          <button type="button" title="단지 상세 닫기" @click="closeApartmentDetail">
            <X :size="20" />
          </button>
        </header>

        <div class="detail-body">
          <section class="detail-hero">
            <div>
              <h3>{{ detailApartment.aptName }}</h3>
              <p>{{ detailAddress }}</p>
              <span>
                아파트 · {{ detailApartment.buildYear || '-' }}년 준공 · 전용
                {{ formatAreaRange(dealStats) }}
              </span>
            </div>
            <div class="detail-actions">
              <button type="button" title="관심 아파트">
                <Heart :size="18" />
              </button>
              <button type="button" title="공유">
                <ExternalLink :size="18" />
              </button>
            </div>
          </section>

          <div v-if="isDetailLoading" class="detail-loading">단지 정보를 불러오는 중...</div>
          <p v-else-if="detailError" class="detail-error">{{ detailError }}</p>

          <template v-else>
            <section class="detail-summary-grid">
              <div>
                <span>매매 거래</span>
                <strong>{{ formatNumber(dealStats.dealCount) }}건</strong>
              </div>
              <div>
                <span>최근 실거래</span>
                <strong>{{ formatKoreanAmount(latestDeal?.dealAmount) }}</strong>
              </div>
              <div>
                <span>3개월 평균</span>
                <strong>{{ formatKoreanAmount(dealStats.recentAverageAmount) }}</strong>
              </div>
              <div>
                <span>최고가</span>
                <strong>{{ formatKoreanAmount(dealStats.highestAmount) }}</strong>
              </div>
            </section>

            <section class="detail-tab-row">
              <button type="button" class="active">시세/실거래가</button>
              <button type="button">단지정보</button>
              <button type="button">이야기</button>
            </section>

            <section class="detail-section">
              <div class="section-heading">
                <h3>시세/실거래가</h3>
                <span>최근 실거래 기준</span>
              </div>
              <div class="latest-price-row">
                <div>
                  <span>최근 실거래</span>
                  <strong>{{ formatKoreanAmount(latestDeal?.dealAmount) }}</strong>
                  <small>{{ formatDealDate(latestDeal) }}</small>
                </div>
                <div>
                  <span>3년 내 최저</span>
                  <strong>{{ formatKoreanAmount(dealStats.lowestAmount) }}</strong>
                </div>
              </div>
            </section>

            <section class="detail-section">
              <div class="section-heading">
                <h3>거래가격 추이</h3>
                <span>최근 12개월 월평균</span>
              </div>

              <div class="trend-stat-row">
                <div>
                  <span>최저가</span>
                  <strong class="blue">{{ formatKoreanAmount(dealStats.lowestAmount) }}</strong>
                </div>
                <div>
                  <span>평균가</span>
                  <strong>{{ formatKoreanAmount(dealStats.averageAmount) }}</strong>
                </div>
                <div>
                  <span>최고가</span>
                  <strong class="red">{{ formatKoreanAmount(dealStats.highestAmount) }}</strong>
                </div>
              </div>

              <div class="trend-chart">
                <svg v-if="trendPoints.length" viewBox="0 0 320 130" role="img" aria-label="거래가격 추이">
                  <polyline :points="trendPolyline" fill="none" stroke="#00b86b" stroke-width="3" />
                  <circle
                    v-for="point in trendPoints"
                    :key="point.label"
                    :cx="point.x"
                    :cy="point.y"
                    r="4"
                    fill="#00b86b"
                  />
                </svg>
                <p v-else>가격 추이 데이터가 없습니다.</p>
              </div>
            </section>

            <section class="detail-section">
              <div class="section-heading">
                <h3>실거래가</h3>
                <span>국토교통부 데이터</span>
              </div>
              <div class="deal-table">
                <div class="deal-table-head">
                  <span>계약일</span>
                  <span>가격</span>
                  <span>면적</span>
                  <span>층</span>
                </div>
                <div v-for="deal in recentDeals" :key="deal.no" class="deal-table-row">
                  <span>{{ formatDealDate(deal) }}</span>
                  <strong>{{ formatKoreanAmount(deal.dealAmount) }}</strong>
                  <span>{{ formatExclusiveArea(deal.exclusiveUseArea) }}</span>
                  <span>{{ formatFloor(deal.floor) }}</span>
                </div>
                <p v-if="!recentDeals.length" class="empty-copy">실거래가 데이터가 없습니다.</p>
              </div>
            </section>
          </template>
        </div>
      </aside>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ChevronRight, ExternalLink, Heart, LocateFixed, X } from 'lucide-vue-next';
import { fetchDongs, fetchGuguns, fetchSidos } from '@/api/regionApi';
import { fetchApartmentDetail, searchApartments } from '@/api/apartmentApi';
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
const isDetailLoading = ref(false);
const detailError = ref('');
const selectedApartment = ref(null);
const selectedApartmentDetail = ref(null);
const mapCenter = ref(DEFAULT_CENTER);
let detailRequestSeq = 0;

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
const mapRegionOptions = computed(() => {
  if (hasSelectedDong.value) {
    return [];
  }

  if (regionStep.value === 'gugun') {
    return guguns.value;
  }

  if (regionStep.value === 'dong') {
    return dongs.value;
  }

  return sidos.value;
});
const detailApartment = computed(() => {
  return selectedApartmentDetail.value?.apartment || selectedApartment.value || {};
});
const dealStats = computed(() => selectedApartmentDetail.value?.dealStats || {});
const latestDeal = computed(() => selectedApartmentDetail.value?.latestDeal || null);
const recentDeals = computed(() => selectedApartmentDetail.value?.recentDeals || []);
const priceTrend = computed(() => selectedApartmentDetail.value?.priceTrend || []);

const detailAddress = computed(() => {
  const apartment = detailApartment.value;
  return [
    apartment.sidoName,
    apartment.gugunName,
    apartment.dongName,
    apartment.jibun ? `지번 ${apartment.jibun}` : null,
  ].filter(Boolean).join(' ');
});

const trendPoints = computed(() => {
  const amounts = priceTrend.value
    .map((point) => Number(point.averageAmount))
    .filter((amount) => amount > 0);

  if (!amounts.length) {
    return [];
  }

  const minAmount = Math.min(...amounts);
  const maxAmount = Math.max(...amounts);
  const chartWidth = 320;
  const chartHeight = 130;
  const padding = 18;
  const safeRange = maxAmount - minAmount || 1;

  return priceTrend.value.map((point, index) => {
    const x = priceTrend.value.length === 1
      ? chartWidth / 2
      : padding + (index / (priceTrend.value.length - 1)) * (chartWidth - padding * 2);
    const y = chartHeight - padding
      - ((Number(point.averageAmount) - minAmount) / safeRange) * (chartHeight - padding * 2);

    return {
      x,
      y,
      label: point.label,
    };
  });
});

const trendPolyline = computed(() => {
  return trendPoints.value.map((point) => `${point.x},${point.y}`).join(' ');
});

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

function formatNumber(value) {
  return Number(value || 0).toLocaleString();
}

function formatKoreanAmount(amount) {
  const value = Number(amount || 0);

  if (!value) {
    return '-';
  }

  const eok = Math.floor(value / 10000);
  const man = value % 10000;

  if (!eok) {
    return `${value.toLocaleString()}만`;
  }

  return man ? `${eok}억 ${man.toLocaleString()}` : `${eok}억`;
}

function formatDealDate(deal) {
  if (!deal?.dealYear || !deal?.dealMonth || !deal?.dealDay) {
    return '-';
  }

  const month = String(deal.dealMonth).padStart(2, '0');
  const day = String(deal.dealDay).padStart(2, '0');
  return `${deal.dealYear}.${month}.${day}`;
}

function formatExclusiveArea(area) {
  const value = Number(area || 0);
  return value ? `${value.toFixed(2).replace(/\\.00$/, '')}㎡` : '-';
}

function formatAreaRange(stats) {
  const minArea = Number(stats?.minExclusiveUseArea || 0);
  const maxArea = Number(stats?.maxExclusiveUseArea || 0);

  if (!minArea && !maxArea) {
    return '-';
  }

  if (minArea === maxArea) {
    return formatExclusiveArea(minArea);
  }

  return `${formatExclusiveArea(minArea)} ~ ${formatExclusiveArea(maxArea)}`;
}

function formatFloor(floor) {
  return floor ? `${floor}층` : '-';
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
  closeApartmentDetail();
  regionStep.value = 'gugun';
  dongListOpen.value = true;
  dongs.value = [];
  guguns.value = await fetchGuguns(sido.name);
  moveMapTo(sido, 8);
  await loadApartments();
}

async function selectGugun(gugun) {
  selectedRegion.gugun = gugun;
  selectedRegion.dong = null;
  closeApartmentDetail();
  regionStep.value = 'dong';
  dongListOpen.value = true;
  dongs.value = await fetchDongs(selectedRegion.sido.name, gugun.name);
  moveMapTo(gugun, 5);
  await loadApartments();
}

async function selectDong(dong) {
  selectedRegion.dong = dong;
  closeApartmentDetail();
  regionStep.value = 'dong';
  dongListOpen.value = false;
  moveMapTo(dong, 4);
  await loadApartments();
}

async function resetToSido() {
  selectedRegion.sido = null;
  selectedRegion.gugun = null;
  selectedRegion.dong = null;
  closeApartmentDetail();
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
  closeApartmentDetail();
  regionStep.value = 'gugun';
  dongListOpen.value = true;
  dongs.value = [];
  moveMapTo(selectedRegion.sido, 8);
  await loadApartments();
}

async function resetToDong() {
  if (!selectedRegion.gugun) {
    return;
  }

  selectedRegion.dong = null;
  closeApartmentDetail();
  regionStep.value = 'dong';
  dongListOpen.value = true;
  moveMapTo(selectedRegion.gugun, 5);
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

async function selectMapRegion(region) {
  // 지도 위 지역 카드를 클릭해도 왼쪽 패널 클릭과 완전히 같은 흐름을 탄다.
  if (regionStep.value === 'sido') {
    await selectSido(region);
    return;
  }

  if (regionStep.value === 'gugun') {
    await selectGugun(region);
    return;
  }

  if (regionStep.value === 'dong') {
    await selectDong(region);
  }
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

async function loadApartmentDetail(aptSeq) {
  const requestId = ++detailRequestSeq;
  isDetailLoading.value = true;
  detailError.value = '';
  selectedApartmentDetail.value = null;

  try {
    const detail = await fetchApartmentDetail(aptSeq);

    if (requestId === detailRequestSeq) {
      selectedApartmentDetail.value = detail;
    }
  } catch (error) {
    if (requestId === detailRequestSeq) {
      detailError.value = '단지 상세 정보를 불러오지 못했습니다.';
    }
  } finally {
    if (requestId === detailRequestSeq) {
      isDetailLoading.value = false;
    }
  }
}

async function selectApartment(apartment) {
  selectedApartment.value = apartment;
  mapCenter.value = {
    lat: apartment.latitude,
    lng: apartment.longitude,
    level: 3,
  };

  // 목록 카드나 지도 마커를 클릭하면 단지 상세 API를 호출해 오른쪽 패널을 채운다.
  await loadApartmentDetail(apartment.aptSeq);
}

function closeApartmentDetail() {
  selectedApartment.value = null;
  selectedApartmentDetail.value = null;
  detailError.value = '';
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
