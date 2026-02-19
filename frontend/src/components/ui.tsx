import React from 'react';
import { clsx } from 'clsx';

// ── Category Pill ─────────────────────────────────────────────────────────────
const CAT_STYLES: Record<string, string> = {
  Food:          'bg-amber-500/15 text-amber-400',
  Transport:     'bg-cyan-500/15 text-cyan-400',
  Shopping:      'bg-violet-500/15 text-violet-400',
  Entertainment: 'bg-pink-500/15 text-pink-400',
  Utilities:     'bg-lime-500/15 text-lime-400',
  Health:        'bg-emerald-500/15 text-emerald-400',
  Finance:       'bg-blue-500/15 text-blue-400',
  Other:         'bg-slate-500/15 text-slate-400',
};

export const CategoryPill: React.FC<{ category: string }> = ({ category }) => (
  <span className={clsx(
    'inline-block px-2.5 py-0.5 rounded-full text-[11px] font-mono font-medium',
    CAT_STYLES[category] ?? CAT_STYLES['Other']
  )}>
    {category}
  </span>
);

// ── Anomaly Badge ─────────────────────────────────────────────────────────────
export const AnomalyBadge: React.FC = () => (
  <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wide bg-red-500/15 text-red-400 border border-red-500/20">
    ⚠ anomaly
  </span>
);

// ── Stat Card ─────────────────────────────────────────────────────────────────
interface StatCardProps {
  label: string;
  value: string;
  sub?: string;
  accent?: 'green' | 'pink' | 'blue' | 'red';
}
const ACCENT_MAP = {
  green: 'from-lime-400/10 border-lime-500/20',
  pink:  'from-pink-500/10 border-pink-500/20',
  blue:  'from-cyan-500/10 border-cyan-500/20',
  red:   'from-red-500/10 border-red-500/20',
};
const VALUE_COLOR = {
  green: 'text-lime-400',
  pink:  'text-pink-400',
  blue:  'text-cyan-400',
  red:   'text-red-400',
};

export const StatCard: React.FC<StatCardProps> = ({ label, value, sub, accent = 'green' }) => (
  <div className={clsx(
    'rounded-2xl border bg-gradient-to-b to-transparent p-5',
    ACCENT_MAP[accent]
  )}>
    <p className="text-[11px] font-semibold uppercase tracking-widest text-slate-400 mb-2">{label}</p>
    <p className={clsx('font-mono text-3xl font-medium leading-none', VALUE_COLOR[accent])}>{value}</p>
    {sub && <p className="text-slate-500 text-xs mt-1.5">{sub}</p>}
  </div>
);

// ── Loading / Empty ───────────────────────────────────────────────────────────
export const Loading: React.FC<{ text?: string }> = ({ text = 'Loading…' }) => (
  <div className="flex items-center justify-center py-16 text-slate-500 font-mono text-sm">{text}</div>
);

export const Empty: React.FC<{ text?: string }> = ({ text = 'No data yet' }) => (
  <div className="flex items-center justify-center py-12 text-slate-600 text-sm">{text}</div>
);

// ── Card wrapper ──────────────────────────────────────────────────────────────
export const Card: React.FC<{ title?: string; children: React.ReactNode; className?: string }> = ({ title, children, className }) => (
  <div className={clsx('rounded-2xl border border-slate-800 bg-slate-900/60 p-6', className)}>
    {title && <p className="text-[11px] font-bold uppercase tracking-[2px] text-slate-500 mb-5">{title}</p>}
    {children}
  </div>
);

// ── Button ────────────────────────────────────────────────────────────────────
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'ghost' | 'danger';
}
const BTN_VARIANTS = {
  primary: 'bg-lime-400 text-slate-950 hover:bg-lime-300 font-bold',
  ghost:   'bg-slate-800 text-slate-300 border border-slate-700 hover:border-lime-400 hover:text-lime-400',
  danger:  'bg-red-500/10 text-red-400 border border-red-500/20 hover:bg-red-500/20',
};

export const Button: React.FC<ButtonProps> = ({ variant = 'primary', className, children, ...props }) => (
  <button
    className={clsx(
      'inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm transition-all duration-150 disabled:opacity-40 disabled:cursor-not-allowed',
      BTN_VARIANTS[variant], className
    )}
    {...props}
  >
    {children}
  </button>
);

// ── Formatters ────────────────────────────────────────────────────────────────
export const fmt = (n: number) =>
  '₹' + n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

export const fmtShort = (n: number) => {
  if (n >= 1e5) return '₹' + (n / 1e5).toFixed(1) + 'L';
  if (n >= 1e3) return '₹' + (n / 1e3).toFixed(1) + 'K';
  return '₹' + n.toFixed(0);
};
