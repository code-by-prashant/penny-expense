import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { PlusCircle, Sparkles } from 'lucide-react';
import { api } from '../api/client';
import { Card, CategoryPill, AnomalyBadge, Button } from '../components/ui';
import type { ExpenseRequest } from '../types';

// Mirror of the backend categorization rules for instant preview
const RULES: [string, string][] = [
  ['uber eats','Food'],['swiggy','Food'],['zomato','Food'],['doordash','Food'],
  ['grubhub','Food'],['instacart','Food'],['mcdonald','Food'],['starbucks','Food'],
  ['subway','Food'],['dominos','Food'],['pizza hut','Food'],['kfc','Food'],
  ['dunkin','Food'],['chipotle','Food'],['panera','Food'],
  ['air india','Transport'],['makemytrip','Transport'],['indigo','Transport'],
  ['spicejet','Transport'],['redbus','Transport'],['irctc','Transport'],
  ['rapido','Transport'],['uber','Transport'],['ola','Transport'],['lyft','Transport'],
  ['metro','Transport'],['airways','Transport'],['airline','Transport'],
  ['amazon','Shopping'],['flipkart','Shopping'],['myntra','Shopping'],
  ['ajio','Shopping'],['nykaa','Shopping'],['walmart','Shopping'],['target','Shopping'],
  ['ebay','Shopping'],['meesho','Shopping'],
  ['prime video','Entertainment'],['apple music','Entertainment'],['netflix','Entertainment'],
  ['spotify','Entertainment'],['hotstar','Entertainment'],['youtube','Entertainment'],
  ['zee5','Entertainment'],['sonyliv','Entertainment'],['steam','Entertainment'],
  ['playstation','Entertainment'],['xbox','Entertainment'],
  ['tata power','Utilities'],['bses','Utilities'],['airtel','Utilities'],['jio','Utilities'],
  ['vodafone','Utilities'],['bsnl','Utilities'],
  ['apollo','Health'],['medplus','Health'],['1mg','Health'],['netmeds','Health'],
  ['pharmeasy','Health'],['cult fit','Health'],['gym','Health'],
  ['insurance','Finance'],['lic','Finance'],['hdfc','Finance'],['icici','Finance'],
  ['sbi','Finance'],['loan','Finance'],['emi','Finance'],
];

function previewCategory(vendor: string): string {
  const lower = vendor.toLowerCase();
  return RULES.find(([k]) => lower.includes(k))?.[1] ?? 'Other';
}

const SAMPLE_RULES = [
  ['Swiggy / Zomato','Food'],['Uber / Ola','Transport'],
  ['Amazon / Flipkart','Shopping'],['Netflix / Spotify','Entertainment'],
  ['Airtel / Jio','Utilities'],['Apollo / 1mg','Health'],
];

export const AddExpense: React.FC<{ onSuccess: () => void }> = ({ onSuccess }) => {
  const today = new Date().toISOString().split('T')[0];
  const [form, setForm] = useState<ExpenseRequest>({
    date: today, amount: 0, vendorName: '', description: '',
  });
  const [catPreview, setCatPreview] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [lastAdded, setLastAdded] = useState<{ category: string; isAnomaly: boolean } | null>(null);

  useEffect(() => {
    if (form.vendorName.length > 1) setCatPreview(previewCategory(form.vendorName));
    else setCatPreview('');
  }, [form.vendorName]);

  const set = (k: keyof ExpenseRequest) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm(prev => ({ ...prev, [k]: k === 'amount' ? parseFloat(e.target.value) || 0 : e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.vendorName.trim() || form.amount <= 0) {
      toast.error('Please fill all required fields'); return;
    }
    setSubmitting(true);
    try {
      const res = await api.createExpense(form);
      toast.success(`Added: ${res.vendorName} → ${res.category}`);
      setLastAdded({ category: res.category, isAnomaly: res.isAnomaly });
      setForm({ date: today, amount: 0, vendorName: '', description: '' });
      onSuccess();
    } catch {
      toast.error('Failed to add expense');
    } finally {
      setSubmitting(false);
    }
  };

  const inputClass = 'w-full bg-slate-800 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-lime-400 transition-colors';

  return (
    <div className="max-w-xl mx-auto space-y-5">
      <Card title="New Expense">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-[11px] font-semibold uppercase tracking-widest text-slate-500 mb-1.5">Date</label>
              <input type="date" value={form.date} onChange={set('date')} className={inputClass} required />
            </div>
            <div>
              <label className="block text-[11px] font-semibold uppercase tracking-widest text-slate-500 mb-1.5">Amount (₹)</label>
              <input type="number" step="0.01" min="0.01" value={form.amount || ''} onChange={set('amount')} placeholder="0.00" className={inputClass} required />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-widest text-slate-500 mb-1.5">Vendor Name</label>
            <input type="text" value={form.vendorName} onChange={set('vendorName')} placeholder="e.g. Swiggy, Amazon, Netflix…" className={inputClass} required />
          </div>

          {catPreview && (
            <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-slate-800 border border-slate-700">
              <Sparkles className="w-3.5 h-3.5 text-lime-400" />
              <span className="text-xs text-slate-400">Auto-detected:</span>
              <CategoryPill category={catPreview} />
            </div>
          )}

          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-widest text-slate-500 mb-1.5">Description <span className="normal-case font-normal">(optional)</span></label>
            <textarea value={form.description} onChange={set('description')} placeholder="Any notes…" rows={2}
              className={inputClass + ' resize-none'} />
          </div>

          <div className="flex gap-3 pt-1">
            <Button type="submit" disabled={submitting}>
              <PlusCircle className="w-4 h-4" />
              {submitting ? 'Adding…' : 'Add Expense'}
            </Button>
          </div>
        </form>

        {lastAdded && (
          <div className="mt-4 p-3 rounded-xl bg-lime-400/5 border border-lime-400/15 flex items-center gap-3">
            <span className="text-lime-400">✓</span>
            <div className="flex items-center gap-2 text-sm">
              <span className="text-slate-400">Saved as</span>
              <CategoryPill category={lastAdded.category} />
              {lastAdded.isAnomaly && <AnomalyBadge />}
            </div>
          </div>
        )}
      </Card>

      <Card title="Vendor → Category Rules">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
          {SAMPLE_RULES.map(([vendor, cat]) => (
            <div key={vendor} className="flex items-center justify-between px-3 py-2 rounded-lg bg-slate-800 border border-slate-800">
              <span className="text-slate-400 text-xs">{vendor}</span>
              <CategoryPill category={cat} />
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};
