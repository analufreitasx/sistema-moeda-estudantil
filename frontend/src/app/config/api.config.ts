type RuntimeEnvironment = {
  API_BASE_URL?: string;
};

declare global {
  interface Window {
    __env?: RuntimeEnvironment;
  }
}

const localApiBaseUrl = 'http://localhost:8080/api/karv-coins';
const runtimeApiBaseUrl = window.__env?.API_BASE_URL?.trim();

export const API_BASE_URL = (runtimeApiBaseUrl || localApiBaseUrl).replace(/\/$/, '');
