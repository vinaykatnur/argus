export function Card({ children, className = '' }) {
  return (
    <div className={`rounded-2xl border border-slate-200 dark:border-white/10 bg-white/70 dark:bg-slate-900/70 shadow-soft backdrop-blur-xl transition-all duration-300 ${className}`}>
      {children}
    </div>
  );
}
