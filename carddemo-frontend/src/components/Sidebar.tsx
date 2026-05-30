import { NavLink } from 'react-router-dom';

const links = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/cards', label: 'Cards' },
  { to: '/transactions', label: 'Transactions' },
  { to: '/reports', label: 'Reports' },
  { to: '/admin', label: 'Admin' },
];

export function Sidebar() {
  return (
    <aside className="w-56 shrink-0 bg-gray-900 text-gray-300">
      <nav className="flex flex-col gap-1 p-4">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `rounded px-3 py-2 text-sm hover:bg-gray-800 hover:text-white ${
                isActive ? 'bg-gray-800 text-white' : ''
              }`
            }
          >
            {link.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
