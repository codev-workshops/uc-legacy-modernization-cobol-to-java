import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { FormField } from '../FormField';

describe('FormField', () => {
  it('renders label and input', () => {
    render(<FormField label="Username" />);
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
  });

  it('shows error message', () => {
    render(<FormField label="Email" error="Invalid email" />);
    expect(screen.getByText('Invalid email')).toBeInTheDocument();
  });

  it('accepts user input', async () => {
    const handler = vi.fn();
    render(<FormField label="Name" onChange={handler} />);
    await userEvent.type(screen.getByLabelText('Name'), 'test');
    expect(handler).toHaveBeenCalled();
  });

  it('renders disabled state', () => {
    render(<FormField label="Disabled" disabled />);
    expect(screen.getByLabelText('Disabled')).toBeDisabled();
  });
});
