import { mkdirSync, writeFileSync } from 'node:fs';

const apiBaseUrl = process.env.API_BASE_URL || 'http://localhost:8080/api/karv-coins';
const content = `window.__env = window.__env || {};\nwindow.__env.API_BASE_URL = ${JSON.stringify(apiBaseUrl)};\n`;

mkdirSync('public', { recursive: true });
writeFileSync('public/env.js', content);
