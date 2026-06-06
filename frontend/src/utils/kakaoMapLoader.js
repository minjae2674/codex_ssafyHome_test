const KAKAO_MAP_SCRIPT_ID = 'kakao-map-sdk';

let kakaoMapSdkPromise = null;

export function loadKakaoMapSdk() {
  const appKey = import.meta.env.VITE_KAKAO_JAVASCRIPT_KEY;

  if (!appKey) {
    return Promise.reject(new Error('Kakao JavaScript 키가 설정되지 않았습니다.'));
  }

  if (window.kakao?.maps) {
    return new Promise((resolve) => {
      window.kakao.maps.load(() => resolve(window.kakao));
    });
  }

  if (kakaoMapSdkPromise) {
    return kakaoMapSdkPromise;
  }

  kakaoMapSdkPromise = new Promise((resolve, reject) => {
    const existingScript = document.getElementById(KAKAO_MAP_SCRIPT_ID);
    const script = existingScript || document.createElement('script');

    script.id = KAKAO_MAP_SCRIPT_ID;
    script.async = true;
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${appKey}&autoload=false`;

    script.onload = () => {
      window.kakao.maps.load(() => resolve(window.kakao));
    };
    script.onerror = () => {
      kakaoMapSdkPromise = null;
      reject(new Error('Kakao 지도 SDK를 불러오지 못했습니다.'));
    };

    if (!existingScript) {
      document.head.appendChild(script);
    }
  });

  return kakaoMapSdkPromise;
}
