import type { ApiErrorResponse } from '../../types/collaboration';

type ErrorPanelProps = {
  error: ApiErrorResponse;
};

export function ErrorPanel({ error }: ErrorPanelProps) {
  return (
    <section className="card error-card" data-testid="error-panel">
      <h2>{error.message}</h2>
      {error.errors.length > 0 && (
        <ul data-testid="error-list">
          {error.errors.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      )}
    </section>
  );
}
