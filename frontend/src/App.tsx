import React, { useState } from 'react';
import { Toaster } from 'react-hot-toast';
import { clsx } from 'clsx';
import { LayoutDashboard, List, PlusCircle, Upload } from 'lucide-react';
import { Dashboard } from './pages/Dashboard';
import { ExpenseList } from './pages/ExpenseList';
import { AddExpense } from './pages/AddExpense';
import { UploadCsv } from './pages/UploadCsv';
import type { Tab } from './types';

const TABS: { id: Tab; label: string; Icon: React.FC<{ className?: string }> }[] = [
  { id: 'dashboard', label: 'Dashboard',   Icon: LayoutDashboard },
  { id: 'expenses',  label: 'Expenses',    Icon: List },
  { id: 'add',       label: 'Add Expense', Icon: PlusCircle },
  { id: 'upload',    label: 'Upload CSV',  Icon: Upload },
];

export default function App() {
  const [tab, setTab] = useState<Tab>('dashboard');
  const [refreshKey, setRefreshKey] = useState(0);

  const onSuccess = () => { setRefreshKey(k => k + 1); setTab('expenses'); };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-200" style={{ fontFamily: "'DM Sans', sans-serif" }}>
      {/* Grid bg */}
      <div className="fixed inset-0 pointer-events-none"
        style={{ backgroundImage: 'linear-gradient(rgba(200,241,53,0.015) 1px, transparent 1px), linear-gradient(90deg, rgba(200,241,53,0.015) 1px, transparent 1px)', backgroundSize: '40px 40px' }} />

      {/* Header */}
      <header className="sticky top-0 z-50 border-b border-slate-800 bg-slate-950/80 backdrop-blur-md">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-lime-400" />
            <span className="font-bold text-lg tracking-tight" style={{ fontFamily: "'Syne', sans-serif" }}>
              broke era<span className="text-lime-400">.</span>
            </span>
          </div>
          <nav className="flex gap-1">
            {TABS.map(({ id, label, Icon }) => (
              <button key={id} onClick={() => setTab(id)}
                className={clsx(
                  'flex items-center gap-2 px-4 py-2 rounded-xl text-sm transition-all',
                  tab === id
                    ? 'bg-lime-400/10 text-lime-400 border border-lime-400/20'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
                )}>
                <Icon className="w-4 h-4" />
                <span className="hidden sm:inline">{label}</span>
              </button>
            ))}
          </nav>
        </div>
      </header>

      {/* Main */}
      <main className="relative max-w-6xl mx-auto px-6 py-8">
        {tab === 'dashboard' && <Dashboard key={`dash-${refreshKey}`} />}
        {tab === 'expenses'  && <ExpenseList key={`list-${refreshKey}`} />}
        {tab === 'add'       && <AddExpense onSuccess={onSuccess} />}
        {tab === 'upload'    && <UploadCsv onSuccess={onSuccess} />}
      </main>

      <Toaster
        position="bottom-right"
        toastOptions={{
          style: { background: '#1e293b', color: '#e2e8f0', border: '1px solid #334155', borderRadius: '12px', fontSize: '13px' },
          success: { iconTheme: { primary: '#c8f135', secondary: '#0f172a' } },
        }}
      />
    </div>
  );
}
