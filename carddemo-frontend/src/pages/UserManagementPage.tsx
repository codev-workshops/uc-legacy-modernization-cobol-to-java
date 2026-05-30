import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { authService } from '../services/authService';
import { DataTable } from '../components/DataTable';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { FormField } from '../components/FormField';
import type { User } from '../types';

type ModalMode = 'closed' | 'add' | 'edit';

export function UserManagementPage() {
  const queryClient = useQueryClient();
  const [mode, setMode] = useState<ModalMode>('closed');
  const [editUser, setEditUser] = useState<User | null>(null);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [userType, setUserType] = useState('');
  const [password, setPassword] = useState('');

  const { data: users, isLoading, error } = useQuery({
    queryKey: ['users'],
    queryFn: authService.getUsers,
  });

  const createMutation = useMutation({
    mutationFn: authService.createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      resetForm();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ userId, data }: { userId: string; data: Partial<User & { password: string }> }) =>
      authService.updateUser(userId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      resetForm();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: authService.deleteUser,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  });

  const resetForm = () => {
    setMode('closed');
    setEditUser(null);
    setFirstName('');
    setLastName('');
    setUserType('');
    setPassword('');
  };

  const openAdd = () => {
    resetForm();
    setMode('add');
  };

  const openEdit = (user: User) => {
    setEditUser(user);
    setFirstName(user.firstName);
    setLastName(user.lastName);
    setUserType(user.userType);
    setPassword('');
    setMode('edit');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (mode === 'add') {
      createMutation.mutate({ firstName, lastName, userType, password });
    } else if (mode === 'edit' && editUser) {
      const data: Partial<User & { password: string }> = {
        firstName,
        lastName,
        userType,
      };
      if (password) data.password = password;
      updateMutation.mutate({ userId: editUser.userId, data });
    }
  };

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="p-4 text-red-600">Failed to load users</div>;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">User Management</h1>
        <button
          onClick={openAdd}
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          Add User
        </button>
      </div>

      <div className="rounded-lg bg-white shadow">
        <DataTable<User>
          columns={[
            { key: 'userId', header: 'User ID' },
            { key: 'firstName', header: 'First Name' },
            { key: 'lastName', header: 'Last Name' },
            { key: 'userType', header: 'Type' },
            {
              key: 'actions',
              header: 'Actions',
              render: (user) => (
                <div className="flex gap-2">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      openEdit(user);
                    }}
                    className="rounded bg-yellow-500 px-2 py-1 text-xs text-white hover:bg-yellow-600"
                  >
                    Edit
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      if (confirm(`Delete user ${user.userId}?`))
                        deleteMutation.mutate(user.userId);
                    }}
                    className="rounded bg-red-500 px-2 py-1 text-xs text-white hover:bg-red-600"
                  >
                    Delete
                  </button>
                </div>
              ),
            },
          ]}
          data={users ?? []}
          keyExtractor={(u) => u.userId}
        />
      </div>

      {mode !== 'closed' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h2 className="mb-4 text-lg font-bold text-gray-800">
              {mode === 'add' ? 'Add User' : 'Edit User'}
            </h2>
            <form onSubmit={handleSubmit}>
              <FormField
                label="First Name"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
              />
              <FormField
                label="Last Name"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
              <FormField
                label="User Type"
                value={userType}
                onChange={(e) => setUserType(e.target.value)}
                required
              />
              <FormField
                label={mode === 'add' ? 'Password' : 'New Password (leave blank to keep)'}
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required={mode === 'add'}
              />
              <div className="flex gap-3">
                <button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                  className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
                >
                  {mode === 'add' ? 'Create' : 'Update'}
                </button>
                <button
                  type="button"
                  onClick={resetForm}
                  className="rounded bg-gray-200 px-4 py-2 text-gray-700 hover:bg-gray-300"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
