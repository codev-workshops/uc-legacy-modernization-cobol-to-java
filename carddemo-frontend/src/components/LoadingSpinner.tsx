export function LoadingSpinner() {
  return (
    <div className="flex items-center justify-center p-8" role="status">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
      <span className="sr-only">Loading...</span>
    </div>
  );
}
