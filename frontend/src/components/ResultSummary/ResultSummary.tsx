import type { AnalyzeResponse } from '../../types/collaboration';

type ResultSummaryProps = {
  result: AnalyzeResponse;
};

export function ResultSummary({ result }: ResultSummaryProps) {
  if (result.employeeId1 === null || result.employeeId2 === null) {
    return (
      <section className="card summary-card" data-testid="no-collaboration">
        <h2>No collaboration found</h2>
        <p>{result.message}</p>
      </section>
    );
  }

  return (
    <section className="card summary-card" data-testid="longest-collaboration">
      <h2>Longest collaboration</h2>
      <div className="summary-grid">
        <div>
          <span className="label">Employee ID #1</span>
          <strong data-testid="employee-id-1">{result.employeeId1}</strong>
        </div>
        <div>
          <span className="label">Employee ID #2</span>
          <strong data-testid="employee-id-2">{result.employeeId2}</strong>
        </div>
        <div>
          <span className="label">Total days worked</span>
          <strong data-testid="total-days-worked">{result.totalDaysWorked}</strong>
        </div>
      </div>
    </section>
  );
}
