interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages: number[] = [];
  const start = Math.max(0, currentPage - 2);
  const end = Math.min(totalPages - 1, currentPage + 2);
  for (let i = start; i <= end; i++) pages.push(i);

  return (
    <nav className="flex items-center justify-center gap-1 py-4" aria-label="Pagination">
      <button
        className="rounded px-3 py-1 text-sm hover:bg-gray-100 disabled:opacity-50"
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        Previous
      </button>
      {pages.map((p) => (
        <button
          key={p}
          className={`rounded px-3 py-1 text-sm ${
            p === currentPage
              ? 'bg-blue-600 text-white'
              : 'hover:bg-gray-100'
          }`}
          onClick={() => onPageChange(p)}
        >
          {p + 1}
        </button>
      ))}
      <button
        className="rounded px-3 py-1 text-sm hover:bg-gray-100 disabled:opacity-50"
        disabled={currentPage >= totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Next
      </button>
    </nav>
  );
}
