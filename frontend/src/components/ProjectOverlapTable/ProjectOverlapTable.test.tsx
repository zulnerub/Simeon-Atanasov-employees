import { render, screen } from '@testing-library/react';
import { ProjectOverlapTable } from './ProjectOverlapTable';
import type { ProjectOverlap } from '../../types/collaboration';

describe('ProjectOverlapTable', () => {
  it('renders nothing when the projects list is empty', () => {
    const { container } = render(<ProjectOverlapTable projects={[]} />);

    expect(container.firstChild).toBeNull();
  });

  it('shows a row for each project', () => {
    const projects: ProjectOverlap[] = [
      { employeeId1: 1, employeeId2: 2, projectId: 10, daysWorked: 50 },
      { employeeId1: 1, employeeId2: 2, projectId: 20, daysWorked: 70 },
    ];

    render(<ProjectOverlapTable projects={projects} />);

    // getAllByRole('row') includes the header row
    expect(screen.getAllByRole('row')).toHaveLength(3);
  });

  it('shows correct data in each row', () => {
    const projects: ProjectOverlap[] = [
      { employeeId1: 3, employeeId2: 5, projectId: 42, daysWorked: 30 },
    ];

    render(<ProjectOverlapTable projects={projects} />);

    const [, dataRow] = screen.getAllByRole('row');
    expect(dataRow).toHaveTextContent('3');
    expect(dataRow).toHaveTextContent('5');
    expect(dataRow).toHaveTextContent('42');
    expect(dataRow).toHaveTextContent('30');
  });
});
