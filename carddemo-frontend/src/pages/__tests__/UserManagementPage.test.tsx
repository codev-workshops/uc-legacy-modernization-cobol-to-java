import { describe, it, expect, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders, loginTestUser } from '../../test/testUtils';
import { UserManagementPage } from '../UserManagementPage';

describe('UserManagementPage', () => {
  it('renders user list', async () => {
    loginTestUser();
    renderWithProviders(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getByText('User Management')).toBeInTheDocument();
    });
    expect(screen.getByText('admin01')).toBeInTheDocument();
    expect(screen.getByText('user01')).toBeInTheDocument();
  });

  it('opens add user modal', async () => {
    loginTestUser();
    renderWithProviders(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getByText('Add User')).toBeInTheDocument();
    });
    await userEvent.click(screen.getByRole('button', { name: 'Add User' }));
    expect(screen.getByLabelText('First Name')).toBeInTheDocument();
    expect(screen.getByLabelText('Last Name')).toBeInTheDocument();
  });

  it('opens edit user modal', async () => {
    loginTestUser();
    renderWithProviders(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getAllByText('Edit').length).toBeGreaterThan(0);
    });
    await userEvent.click(screen.getAllByText('Edit')[0]);
    expect(screen.getByText('Edit User')).toBeInTheDocument();
  });

  it('submits add user form', async () => {
    loginTestUser();
    renderWithProviders(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Add User' })).toBeInTheDocument();
    });
    await userEvent.click(screen.getByRole('button', { name: 'Add User' }));
    await userEvent.type(screen.getByLabelText('First Name'), 'New');
    await userEvent.type(screen.getByLabelText('Last Name'), 'User');
    await userEvent.type(screen.getByLabelText('User Type'), 'USER');
    await userEvent.type(screen.getByLabelText('Password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => {
      expect(screen.queryByLabelText('First Name')).not.toBeInTheDocument();
    });
  });

  it('submits edit user form', async () => {
    loginTestUser();
    renderWithProviders(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getAllByText('Edit').length).toBeGreaterThan(0);
    });
    await userEvent.click(screen.getAllByText('Edit')[0]);
    await userEvent.clear(screen.getByLabelText('First Name'));
    await userEvent.type(screen.getByLabelText('First Name'), 'Updated');
    await userEvent.click(screen.getByRole('button', { name: 'Update' }));

    await waitFor(() => {
      expect(screen.queryByText('Edit User')).not.toBeInTheDocument();
    });
  });

  it('deletes a user', async () => {
    loginTestUser();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    renderWithProviders(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getAllByText('Delete').length).toBeGreaterThan(0);
    });
    await userEvent.click(screen.getAllByText('Delete')[0]);
    expect(window.confirm).toHaveBeenCalled();
    vi.restoreAllMocks();
  });

  it('cancels add user modal', async () => {
    loginTestUser();
    renderWithProviders(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Add User' })).toBeInTheDocument();
    });
    await userEvent.click(screen.getByRole('button', { name: 'Add User' }));
    expect(screen.getByLabelText('First Name')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.queryByLabelText('First Name')).not.toBeInTheDocument();
  });
});
