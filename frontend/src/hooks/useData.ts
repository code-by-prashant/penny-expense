import { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';
import type { Expense, DashboardResponse } from '../types';

// Generic fetch hook
function useFetch<T>(fetcher: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await fetcher());
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => { load(); }, [load]);

  return { data, loading, error, refetch: load };
}

export function useExpenses() {
  return useFetch<Expense[]>(() => api.listExpenses());
}

export function useDashboard() {
  return useFetch<DashboardResponse>(() => api.getDashboard());
}
