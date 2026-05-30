import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DataTable } from '../DataTable';

interface Item {
  id: number;
  name: string;
}

const columns = [
  { key: 'id', header: 'ID' },
  { key: 'name', header: 'Name' },
];

const data: Item[] = [
  { id: 1, name: 'Alice' },
  { id: 2, name: 'Bob' },
];

describe('DataTable', () => {
  it('renders headers and rows', () => {
    render(
      <DataTable<Item>
        columns={columns}
        data={data}
        keyExtractor={(i) => i.id}
      />,
    );
    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
  });

  it('shows empty message when no data', () => {
    render(
      <DataTable<Item>
        columns={columns}
        data={[]}
        keyExtractor={(i) => i.id}
      />,
    );
    expect(screen.getByText('No data available')).toBeInTheDocument();
  });

  it('calls onRowClick when a row is clicked', async () => {
    const handler = vi.fn();
    render(
      <DataTable<Item>
        columns={columns}
        data={data}
        keyExtractor={(i) => i.id}
        onRowClick={handler}
      />,
    );
    await userEvent.click(screen.getByText('Alice'));
    expect(handler).toHaveBeenCalledWith(data[0]);
  });

  it('renders custom column renderer', () => {
    render(
      <DataTable<Item>
        columns={[
          { key: 'id', header: 'ID' },
          {
            key: 'name',
            header: 'Name',
            render: (i) => <strong>{i.name}</strong>,
          },
        ]}
        data={data}
        keyExtractor={(i) => i.id}
      />,
    );
    const bold = screen.getByText('Alice');
    expect(bold.tagName).toBe('STRONG');
  });

  it('renders pagination when props provided', () => {
    render(
      <DataTable<Item>
        columns={columns}
        data={data}
        keyExtractor={(i) => i.id}
        currentPage={0}
        totalPages={3}
        onPageChange={vi.fn()}
      />,
    );
    expect(screen.getByText('Next')).toBeInTheDocument();
  });
});
