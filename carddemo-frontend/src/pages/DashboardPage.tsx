import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const menuItems = [
  { to: '/cards', title: 'Cards', description: 'View and manage credit cards' },
  { to: '/transactions', title: 'Transactions', description: 'View and create transactions' },
  { to: '/reports', title: 'Reports', description: 'Generate transaction reports' },
  { to: '/admin', title: 'Administration', description: 'Manage users and system settings' },
];

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">
        Welcome, {user?.firstName ?? 'User'}
      </h1>
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {menuItems.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className="rounded-lg bg-white p-6 shadow hover:shadow-md transition-shadow"
          >
            <h2 className="mb-2 text-lg font-semibold text-blue-700">
              {item.title}
            </h2>
            <p className="text-sm text-gray-600">{item.description}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
