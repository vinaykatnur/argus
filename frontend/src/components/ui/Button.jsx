export function Button({ children, variant = 'primary', className = '', ...props }) {
  const base = 'inline-flex items-center justify-center rounded-full px-4 py-2 text-sm font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary/50';
  const variants = {
    primary: 'bg-primary text-white hover:bg-primary/90 active:scale-95 transition-all duration-150',
    secondary: 'border border-slate-200 dark:border-white/10 bg-slate-100 dark:bg-white/5 text-slate-900 dark:text-slate-100 hover:bg-slate-200 dark:hover:bg-white/10 active:scale-95 transition-all duration-150',
    ghost: 'text-slate-600 dark:text-slate-300 hover:bg-slate-200/50 dark:hover:bg-white/5 hover:text-slate-900 dark:hover:text-white active:scale-95 transition-all duration-150',
  };

  return (
    <button className={`${base} ${variants[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
}
