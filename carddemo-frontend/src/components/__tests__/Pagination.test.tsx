import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Pagination } from '../Pagination';

describe('Pagination', () => {
  it('does not render when totalPages <= 1', () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={1} onPageChange={vi.fn()} />,
    );
    expect(container.innerHTML).toBe('');
  });

  it('renders page buttons', () => {
    render(
      <Pagination currentPage={0} totalPages={5} onPageChange={vi.fn()} />,
    );
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('Previous')).toBeDisabled();
    expect(screen.getByText('Next')).not.toBeDisabled();
  });

  it('calls onPageChange when clicking a page', async () => {
    const handler = vi.fn();
    render(
      <Pagination currentPage={0} totalPages={5} onPageChange={handler} />,
    );
    await userEvent.click(screen.getByText('2'));
    expect(handler).toHaveBeenCalledWith(1);
  });

  it('calls onPageChange for Next', async () => {
    const handler = vi.fn();
    render(
      <Pagination currentPage={1} totalPages={5} onPageChange={handler} />,
    );
    await userEvent.click(screen.getByText('Next'));
    expect(handler).toHaveBeenCalledWith(2);
  });

  it('disables Next on last page', () => {
    render(
      <Pagination currentPage={4} totalPages={5} onPageChange={vi.fn()} />,
    );
    expect(screen.getByText('Next')).toBeDisabled();
  });
});
