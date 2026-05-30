import { Link } from 'react-router-dom';

export function AdminPage() {
  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-800">Administration</h1>
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <Link
          to="/admin/users"
          className="rounded-lg bg-white p-6 shadow hover:shadow-md transition-shadow"
        >
          <h2 className="mb-2 text-lg font-semibold text-blue-700">
            User Management
          </h2>
          <p className="text-sm text-gray-600">
            Add, edit, and delete system users
          </p>
        </Link>
        <Link
          to="/reports"
          className="rounded-lg bg-white p-6 shadow hover:shadow-md transition-shadow"
        >
          <h2 className="mb-2 text-lg font-semibold text-blue-700">Reports</h2>
          <p className="text-sm text-gray-600">
            Generate and view transaction reports
          </p>
        </Link>
      </div>
    </div>
  );
}
