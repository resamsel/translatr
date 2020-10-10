export interface LoadGeneratorConfig {
  baseUrl: string;
  accessToken: string;
  usersPerMinute: number;
  includePersonas: string[];
  maxRetryAttempts: number;
  retryScalingDelay: number;
}
