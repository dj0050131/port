import Mock from 'mockjs';
import type { ResponseResult } from '@/types/common';

const success = <T>(data: T): ResponseResult<T> => ({
  code: 200,
  message: 'success',
  data,
  timestamp: Date.now()
});

const parseQuery = (url = '') => {
  const search = url.split('?')[1];
  const params: Record<string, any> = {};
  if (!search) return params;
  search.split('&').forEach(item => {
    const [key, value] = item.split('=');
    params[key] = decodeURIComponent(value);
  });
  return params;
};

Mock.setup({ timeout: '500-800' });

Mock.mock(/\/mock\/api\/v1\/pilot\/apply\/list/, 'get', options => {
  const { page = 1, size = 10 } = parseQuery(options?.url);
  return success({
    total: 100,
    list: Mock.mock({
      [`array|${size}`]: [
        {
          applyNo: '@string("upper", 10)',
          shipName: '@cword(4, 7)',
          shipNetTonnage: '@integer(1200, 90000)',
          applyTime: '@datetime("yyyy-MM-dd HH:mm:ss")',
          status: '@integer(0,2)',
          agentName: '@cword(4,6)'
        }
      ]
    }).array
  });
});

Mock.mock(/\/mock\/api\/v1\/pilot\/plan\/list/, 'get', () =>
  success(
    Mock.mock({
      'array|8': [
        {
          planNo: '@string("upper", 8)',
          shipName: '@cword(4,7)',
          pilotName: '@cname',
          startTime: '@datetime("yyyy-MM-dd HH:mm")',
          endTime: '@datetime("yyyy-MM-dd HH:mm")',
          'status|1': ['pending', 'running', 'done']
        }
      ]
    }).array
  )
);

Mock.mock(/\/mock\/api\/v1\/pilot\/plan\/real-time/, 'get', () =>
  success({
    activePlans: 8,
    completedToday: Mock.Random.integer(12, 24),
    alerts: Mock.Random.integer(0, 3),
    operations: Mock.mock({
      'array|6': [
        {
          shipName: '@cword(4,7)',
          action: '@cword(4,6)调整',
          operator: '@cname',
          time: '@datetime("yyyy-MM-dd HH:mm:ss")'
        }
      ]
    }).array
  })
);

Mock.mock(/\/mock\/api\/v1\/map\/ship\/location/, 'get', () =>
  success(
    Mock.mock({
      'array|6': [
        {
          id: '@id',
          name: '@cword(4,7)号',
          lnglat: () => [116.3 + Math.random() * 0.1, 39.9 + Math.random() * 0.1],
          'status|1-2': 1
        }
      ]
    }).array
  )
);

Mock.mock(/\/mock\/api\/v1\/fee\/rate\/list/, 'get', () =>
  success([
    { id: '1', shipType: '散货船', baseRate: 0.75, holidayExtra: 20, remark: '常规' },
    { id: '2', shipType: '集装箱船', baseRate: 0.95, holidayExtra: 25, remark: '航线优选' }
  ])
);

Mock.mock(/\/mock\/api\/v1\/fee\/rate\/save/, 'post', () => success(true));

Mock.mock(/\/mock\/api\/v1\/fee\/calculate/, 'post', () =>
  success({
    visaNo: 'VISA-MOCK',
    totalFee: 86000,
    breakdown: {
      base: 62000,
      holiday: 12000,
      discount: -2000
    }
  })
);

Mock.mock(/\/mock\/api\/v1\/statistic\/pilot\/trend/, 'get', () =>
  success({
    months: Array.from({ length: 12 }, (_, i) => `${i + 1}月`),
    counts: Array.from({ length: 12 }, () => Mock.Random.integer(40, 120))
  })
);

Mock.mock(/\/mock\/api\/v1\/statistic\/fee\/overview/, 'get', () =>
  success({
    total: 12600000,
    monthOverMonth: 8.6,
    categories: [
      { name: '引航费', value: 7200000 },
      { name: '靠泊费', value: 3800000 },
      { name: '其他', value: 1600000 }
    ]
  })
);

