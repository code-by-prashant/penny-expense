import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { Trash2, Search } from 'lucide-react';
import { useExpenses } from '../hooks/useData';
import { api } from '../api/client';
import { Card, Loading, Empty, CategoryPill, AnomalyBadge, Button, fmt } from '../components/ui';
import { clsx } from 'clsx';

const CATEGORIES = ['All','Food','Transport','Shopping','Entertainment','Utilities','Health','Finance','Other'];

export const ExpenseList: React.FC = () => {
  const { data: expenses, loading, error, refetch } = useExpenses();
  const [search, setSearch] = useState('');
  const [cat, setCat] = useState('All');
  const [deleting, setDeleting] = useState<number | null>(null);

  const filtered = (expenses ?? []).filter(e => {
    const matchCat = cat === 'All' || e.category === cat;
    const q = search.toLowerCase();
    const matchSearch = !q || e.vendorName.toLowerCase().includes(q) ||
      e.category.toLowerCase().includes(q) || (e.description ?? '').toLowerCase().includes(q);
    return matchCat && matchSearch;
  });

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this expense?')) return;
    setDeleting(id);
    try {
      await api.deleteExpense(id);
      toast.success('Expense deleted');
      refetch();
    } catch {
      toast.error('Failed to delete');
    } finally {
      setDeleting(null);
    }
  };

  if (loading) return <Loading text="Loading expenses…" />;
  if (error) return <div className="text-red-400 p-8 font-mono text-sm">Error: {error}</div>;

  return (
    <Card>
      {/* Controls */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 w-4 h-4" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search vendor, category, description…"
            className="w-full bg-slate-800 border border-slate-700 rounded-xl pl-9 pr-4 py-2.5 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-lime-400"
          />
        </div>
        <select
          value={cat}
          onChange={e => setCat(e.target.value)}
          className="bg-slate-800 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-lime-400"
        >
          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>

      <p className="text-slate-500 text-xs mb-4 font-mono">{filtered.length} expenses</p>

      {filtered.length === 0 ? <Empty text="No expenses match your filters" /> : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-800">
                {['Date','Vendor','Category','Description','Amount','Status',''].map(h => (
                  <th key={h} className="text-left pb-3 px-3 text-[11px] font-semibold uppercase tracking-wider text-slate-500 first:pl-0 last:pr-0">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map(e => (
                <tr key={e.id}
                  className={clsx('border-b border-slate-800/60 transition-colors hover:bg-slate-800/30',
                    e.isAnomaly && 'bg-red-500/5 hover:bg-red-500/10')}>
                  <td className="py-3 px-3 pl-0 font-mono text-xs text-slate-400">{e.date}</td>
                  <td className="py-3 px-3 font-medium text-slate-200">{e.vendorName}</td>
                  <td className="py-3 px-3"><CategoryPill category={e.category} /></td>
                  <td className="py-3 px-3 text-slate-400 max-w-[180px] truncate">{e.description || '—'}</td>
                  <td className="py-3 px-3 font-mono font-semibold text-slate-200">{fmt(e.amount)}</td>
                  <td className="py-3 px-3">
                    {e.isAnomaly
                      ? <AnomalyBadge />
                      : <span className="text-slate-600 text-xs">normal</span>}
                  </td>
                  <td className="py-3 pr-0">
                    <Button
                      variant="danger"
                      className="!px-2.5 !py-1.5"
                      onClick={() => handleDelete(e.id)}
                      disabled={deleting === e.id}
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
};
