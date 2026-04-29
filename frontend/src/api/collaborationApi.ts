import type { AnalyzeResponse, ApiErrorResponse } from '../types/collaboration';

export async function analyzeFile(file: File): Promise<AnalyzeResponse> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/collaborations/analyze', {
    method: 'POST',
    body: formData
  });

  if (!response.ok) {
    let apiError: ApiErrorResponse;
    try {
      apiError = await response.json();
    } catch {
      apiError = {
       message: 'The file could not be analyzed.',
       errors: ['Unexpected error while communicating with the server.']
      };
    }

    throw apiError;
  }

  return response.json();
}
