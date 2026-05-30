import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { Reports } from '../pages/Reports';
import { AuthProvider } from '../context/AuthContext';
import { setTokenGetter } from '../api/client';
import { beforeAll, afterAll, afterEach, describe, it, expect } from 'vitest';

const mockReports = [
  {
    id: 1,
    accountId: 12345,
    customerName: 'John Doe',
    reportType: 'STATEMENT',
    textContent: 'Sample text statement content',
    htmlContent: '<html>Sample</html>',
    generatedAt: '2024-06-15T10:00:00',
  },
];

const server = setupServer(
  http.get('/api/reports', () => {
    return HttpResponse.json(mockReports);
  }),
  http.post('/api/reports/generate', () => {
    return HttpResponse.json({ status: 'COMPLETED' });
  })
);

beforeAll(() => {
  server.listen();
  setTokenGetter(() => 'mock-token');
});
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

function renderReports() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/transaction-reports']}>
        <Routes>
          <Route path="/transaction-reports" element={<Reports />} />
          <Route path="/menu" element={<div>Main Menu</div>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe('Reports', () => {
  it('should render reports page title', async () => {
    renderReports();
    expect(screen.getByText('Transaction Reports')).toBeInTheDocument();
  });

  it('should display generate button', async () => {
    renderReports();
    expect(screen.getByRole('button', { name: 'Generate Statements' })).toBeInTheDocument();
  });

  it('should load and display reports list', async () => {
    renderReports();
    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });
    expect(screen.getByText('12345')).toBeInTheDocument();
    expect(screen.getByText('STATEMENT')).toBeInTheDocument();
  });

  it('should show report detail when view is clicked', async () => {
    renderReports();
    const user = userEvent.setup();
    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });
    await user.click(screen.getByRole('button', { name: 'View' }));
    await waitFor(() => {
      expect(screen.getByText(/Sample text statement content/)).toBeInTheDocument();
    });
  });

  it('should show empty state when no reports', async () => {
    server.use(
      http.get('/api/reports', () => {
        return HttpResponse.json([]);
      })
    );
    renderReports();
    await waitFor(() => {
      expect(screen.getByText('No reports generated yet.')).toBeInTheDocument();
    });
  });

  it('should trigger report generation', async () => {
    renderReports();
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Generate Statements' }));
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Generate Statements' })).not.toBeDisabled();
    });
  });

  it('should show back to menu link', () => {
    renderReports();
    expect(screen.getByText('Back to Main Menu')).toBeInTheDocument();
  });
});
