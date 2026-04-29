import type { ProjectOverlap } from '../../types/collaboration';

type ProjectOverlapTableProps = {
  projects: ProjectOverlap[];
};

export function ProjectOverlapTable({ projects }: ProjectOverlapTableProps) {
  if (projects.length === 0) {
    return null;
  }

  return (
    <section className="card table-card">
      <h2>Common projects</h2>
      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Employee ID #1</th>
              <th>Employee ID #2</th>
              <th>Project ID</th>
              <th>Days worked</th>
            </tr>
          </thead>
          <tbody>
            {projects.map((project) => (
              <tr key={`${project.employeeId1}-${project.employeeId2}-${project.projectId}`}>
                <td>{project.employeeId1}</td>
                <td>{project.employeeId2}</td>
                <td>{project.projectId}</td>
                <td>{project.daysWorked}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
