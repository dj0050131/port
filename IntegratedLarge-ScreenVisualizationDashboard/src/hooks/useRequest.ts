import { ref } from 'vue';

export const useRequest = <TData, TParams extends any[] = []>(
  service: (...args: TParams) => Promise<TData>,
  immediate = true,
  ...args: TParams
) => {
  const loading = ref(false);
  const data = ref<TData | null>(null);
  const error = ref<unknown>(null);

  const run = async (...runArgs: TParams) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await service(...runArgs);
      data.value = response;
      return response;
    } catch (err) {
      error.value = err;
      throw err;
    } finally {
      loading.value = false;
    }
  };

  if (immediate) {
    run(...args);
  }

  return {
    data,
    loading,
    error,
    run
  };
};

