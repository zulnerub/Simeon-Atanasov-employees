export type ProjectOverlap = {
  employeeId1: number;
  employeeId2: number;
  projectId: number;
  daysWorked: number;
};

export type AnalyzeResponse = {
  employeeId1: number | null;
  employeeId2: number | null;
  totalDaysWorked: number;
  message: string;
  projects: ProjectOverlap[];
};

export type ApiErrorResponse = {
  message: string;
  errors: string[];
};
