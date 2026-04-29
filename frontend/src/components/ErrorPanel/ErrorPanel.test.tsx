import { render, screen } from '@testing-library/react';
import { ErrorPanel } from './ErrorPanel';

describe('ErrorPanel', () => {
  it('shows the error message', () => {
    render(<ErrorPanel error={{ message: 'Something went wrong', errors: [] }} />);

    expect(screen.getByTestId('error-panel')).toHaveTextContent('Something went wrong');
  });

  it('shows each error item when errors are provided', () => {
    render(<ErrorPanel error={{ message: 'Validation failed', errors: ['Field A is invalid', 'Field B is required'] }} />);

    const list = screen.getByTestId('error-list');
    expect(list).toHaveTextContent('Field A is invalid');
    expect(list).toHaveTextContent('Field B is required');
  });

  it('does not show the error list when errors array is empty', () => {
    render(<ErrorPanel error={{ message: 'Something went wrong', errors: [] }} />);

    expect(screen.queryByTestId('error-list')).not.toBeInTheDocument();
  });
});
