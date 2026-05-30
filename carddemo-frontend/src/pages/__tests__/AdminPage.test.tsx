import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../../test/testUtils';
import { AdminPage } from '../AdminPage';

describe('AdminPage', () => {
  it('renders admin title', () => {
    renderWithProviders(<AdminPage />);
    expect(screen.getByText('Administration')).toBeInTheDocument();
  });

  it('renders user management link', () => {
    renderWithProviders(<AdminPage />);
    expect(screen.getByText('User Management')).toBeInTheDocument();
  });

  it('renders reports link', () => {
    renderWithProviders(<AdminPage />);
    expect(screen.getByText('Reports')).toBeInTheDocument();
  });
});
