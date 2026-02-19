import React, { useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import { useDashboard } from '../hooks/useData';
import { Card, StatCard, Loading, Empty, AnomalyBadge, CategoryPill, fmt, fmtShort } from '../components/ui';
import { clsx } from 'clsx';

const COLORS = ['#c8f135','#f13580','#35c8f1','#f1a235','#a135f1','#35f1a2','#3580f1','#6b7591'];

const CustomTooltip = ({ active, payload, label }: { active?: boolean; payload?: { value: number }[]; label?: string }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs">
      <p className="text-slate-400 mb-1">{label}</p>
      <p className="text-lime-400 font-mono font-semibold">{fmtShort(payload[0].value)}</p>
    </div>
  );
};

export const Dashboard: React.FC = () => {
  const { data, loading, error } = useDashboard();
  const [selectedMonth, setSelectedMonth] = useState<string | null>(null);

  if (loading) return <Loading text="Fetching dashboard…" />;
  if (error) return <div className="text-red-400 p-8 font-mono text-sm">Error: {error}</div>;
  if (!data) return <Empty />;

  const months = Object.keys(data.monthlyByCategory).sort().reverse();
  const activeMonth = selectedMonth ?? months[0] ?? '';
  const monthData = data.monthlyByCategory[activeMonth] ?? {};

  const totalSpend = data.categoryTotals.reduce((a, r) => a + r.total, 0);
  const monthTotal = Object.values(monthData).reduce((a, v) => a + v, 0);

  // Pie data
  const pieData = data.categoryTotals.map(c => ({ name: c.category, value: c.total }));

  // Bar data for selected month
  const barData = Object.entries(monthData)
    .sort((a, b) => b[1] - a[1])
    .map(([cat, val]) => ({ name: cat, amount: val }));

  // Top vendors bar data
  const vendorBar = data.topVendors.map(v => ({ name: v.vendorName, amount: v.total }));

  return (
    <div className="space-y-5">
      {/* Stat Row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard label="Total Spend" value={fmtShort(totalSpend)} sub={`${data.categoryTotals.reduce((a,c)=>a+c.count,0)} transactions`} accent="green" />
        <StatCard label="This Month" value={fmtShort(monthTotal)} sub={activeMonth || '—'} accent="blue" />
        <StatCard label="Categories" value={String(data.categoryTotals.length)} sub="active" accent="pink" />
        <StatCard label="Anomalies" value={String(data.anomalyCount)} sub="flagged" accent="red" />
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Category Pie */}
        <Card title="All-Time by Category">
          {pieData.length ? (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100}
                  paddingAngle={3} dataKey="value">
                  {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip formatter={(v: number) => fmtShort(v)} contentStyle={{ background:'#1e2535', border:'1px solid #2d3748', borderRadius:12 }} />
                <Legend iconType="circle" iconSize={8} formatter={(v) => <span className="text-slate-400 text-xs">{v}</span>} />
              </PieChart>
            </ResponsiveContainer>
          ) : <Empty />}
        </Card>

        {/* Top Vendors Bar */}
        <Card title="Top 5 Vendors">
          {vendorBar.length ? (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={vendorBar} layout="vertical" margin={{ left: 10, right: 20 }}>
                <XAxis type="number" hide />
                <YAxis type="category" dataKey="name" width={110} tick={{ fill:'#94a3b8', fontSize:12 }} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="amount" fill="#c8f135" radius={[0,6,6,0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <Empty />}
        </Card>
      </div>

      {/* Monthly breakdown */}
      <Card title="Monthly Breakdown">
        {/* Month tabs */}
        <div className="flex gap-2 flex-wrap mb-5">
          {months.slice(0, 8).map(m => (
            <button key={m} onClick={() => setSelectedMonth(m)}
              className={clsx(
                'px-3 py-1.5 rounded-lg text-xs font-mono border transition-all',
                m === activeMonth
                  ? 'bg-lime-400/10 border-lime-400/30 text-lime-400'
                  : 'bg-slate-800 border-slate-700 text-slate-400 hover:border-slate-500'
              )}>
              {m}
            </button>
          ))}
        </div>
        {barData.length ? (
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={barData}>
              <XAxis dataKey="name" tick={{ fill:'#94a3b8', fontSize:11 }} />
              <YAxis tick={{ fill:'#94a3b8', fontSize:11 }} tickFormatter={fmtShort} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="amount" radius={[6,6,0,0]}>
                {barData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        ) : <Empty text="No data for this month" />}
      </Card>

      {/* Anomalies */}
      <Card title="Anomalies Detected" className={data.anomalyCount > 0 ? 'border-red-500/20' : ''}>
        {data.anomalies.length === 0 ? (
          <div className="flex items-center gap-2 text-emerald-400 text-sm py-4">
            <span>🟢</span> No anomalies detected — all expenses look normal.
          </div>
        ) : (
          <div className="space-y-3">
            {data.anomalies.map(a => (
              <div key={a.id} className="flex items-center justify-between p-3 rounded-xl bg-red-500/5 border border-red-500/10">
                <div>
                  <p className="font-medium text-slate-200">{a.vendorName}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-slate-500 text-xs font-mono">{a.date}</span>
                    <CategoryPill category={a.category} />
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-mono font-semibold text-red-400">{fmt(a.amount)}</p>
                  <AnomalyBadge />
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
};
