import { render, screen } from '@testing-library/react';
import { ResultSummary } from './ResultSummary';
import type { AnalyzeResponse } from '../../types/collaboration';

const baseResult: AnalyzeResponse = {
  employeeId1: null,
  employeeId2: null,
  totalDaysWorked: 0,
  message: '',
  projects: [],
};

describe('ResultSummary', () => {
  it('shows no collaboration message when no pair was found', () => {
    const result: AnalyzeResponse = {
      ...baseResult,
      message: 'No employees worked on the same project.',
    };

    render(<ResultSummary result={result} />);

    const section = screen.getByTestId('no-collaboration');
    expect(section).toBeInTheDocument();
    expect(section).toHaveTextContent('No collaboration found');
    expect(section).toHaveTextContent('No employees worked on the same project.');
  });

  it('shows the longest collaboration details when a pair was found', () => {
    const result: AnalyzeResponse = {
      ...baseResult,
      employeeId1: 1,
      employeeId2: 2,
      totalDaysWorked: 120,
    };

    render(<ResultSummary result={result} />);

    expect(screen.getByTestId('longest-collaboration')).toBeInTheDocument();
    expect(screen.getByTestId('employee-id-1')).toHaveTextContent('1');
    expect(screen.getByTestId('employee-id-2')).toHaveTextContent('2');
    expect(screen.getByTestId('total-days-worked')).toHaveTextContent('120');
  });
});
