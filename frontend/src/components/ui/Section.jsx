export function Section({ eyebrow, title, description, children, className = '' }) {
  return (
    <section className={`mx-auto w-full max-w-7xl px-6 py-20 sm:px-8 lg:px-10 ${className}`}>
      <div className="mb-10 max-w-2xl">
        {eyebrow ? <p className="mb-3 text-sm font-semibold uppercase tracking-[0.28em] text-primary">{eyebrow}</p> : null}
        <h2 className="text-3xl font-semibold tracking-tight text-white sm:text-4xl">{title}</h2>
        {description ? <p className="mt-4 text-lg text-slate-400">{description}</p> : null}
      </div>
      {children}
    </section>
  );
}
