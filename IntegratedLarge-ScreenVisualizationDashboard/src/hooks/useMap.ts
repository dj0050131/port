import { onMounted, onUnmounted, ref } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';
import type { ShipLocation } from '@/types/pilot';

declare const AMap: any;

export const useMap = () => {
  const mapRef = ref<HTMLDivElement | null>(null);
  const mapInstance = ref<any>(null);
  const markers = ref<any[]>([]);
  const pendingShips = ref<ShipLocation[] | null>(null);

  const initMap = async () => {
    const loaderConfig: any = {
      key: import.meta.env.VITE_AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.MarkerClusterer']
    };
    if (import.meta.env.VITE_AMAP_SECURITY) {
      loaderConfig.securityJsCode = import.meta.env.VITE_AMAP_SECURITY;
    }

    await AMapLoader.load(loaderConfig);

    if (!mapRef.value) return;

    mapInstance.value = new AMap.Map(mapRef.value, {
      zoom: 12,
      center: [121.48, 31.23],
      viewMode: '3D',
      mapStyle: 'amap://styles/blue',
      pitch: 45,
      rotation: -15
    });

    mapInstance.value.addControl(new AMap.Scale({ position: 'LB' }));
    mapInstance.value.addControl(new AMap.ToolBar({ position: 'RB' }));
    if (pendingShips.value?.length) {
      loadShipMarkers(pendingShips.value);
      pendingShips.value = null;
    }
  };

  const clearMarkers = () => {
    if (!mapInstance.value) return;
    markers.value.forEach((marker: any) => {
      mapInstance.value?.remove(marker);
    });
    markers.value = [];
  };

  const loadShipMarkers = (ships?: ShipLocation[]) => {
    if (!ships?.length) {
      clearMarkers();
      return;
    }
    if (!mapInstance.value) {
      pendingShips.value = ships;
      return;
    }
    clearMarkers();
    ships.forEach(ship => {
      const marker = new AMap.Marker({
        position: ship.lnglat,
        title: ship.name,
        icon: new AMap.Icon({
          size: new AMap.Size(32, 32),
          imageSize: new AMap.Size(32, 32),
          image:
            ship.status === 1
              ? new URL('../assets/images/ship-active.png', import.meta.url).href
              : new URL('../assets/images/ship-inactive.png', import.meta.url).href
        })
      });
      marker.on('click', () => {
        window.dispatchEvent(
          new CustomEvent('ship-marker-click', {
            detail: ship
          })
        );
      });
      markers.value.push(marker);
      mapInstance.value?.add(marker);
    });
  };

  onMounted(() => {
    initMap();
  });

  onUnmounted(() => {
    if (mapInstance.value) {
      mapInstance.value.destroy();
    }
  });

  return {
    mapRef,
    mapInstance,
    loadShipMarkers
  };
};
