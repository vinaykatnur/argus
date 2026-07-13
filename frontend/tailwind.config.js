/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: '#050816',
        surface: '#0f172a',
        primary: 'var(--primary-color, #4F8CFF)',
        success: '#22C55E',
        warning: '#F59E0B',
        danger: '#EF4444',
        accent: 'var(--accent-color, #A78BFA)',
      },
      boxShadow: {
        soft: '0 20px 60px -24px rgba(15, 23, 42, 0.45)',
      },
      animation: {
        'float-slow': 'float 18s ease-in-out infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-8px)' },
        },
      },
    },
  },
  plugins: [],
};
