import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { PlatformPreferencesProvider } from './context/PlatformPreferencesContext';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: false,
    },
  },
});

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <PlatformPreferencesProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </PlatformPreferencesProvider>
    </QueryClientProvider>
  </StrictMode>,
);
