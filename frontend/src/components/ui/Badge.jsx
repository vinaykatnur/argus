export function Badge({ children, className = '' }) {
  return (
    <span className={`inline-flex items-center rounded-full border border-slate-200 dark:border-white/10 bg-slate-100 dark:bg-white/5 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-600 dark:text-slate-300 transition-all duration-300 ${className}`}>
      {children}
    </span>
  );
}
