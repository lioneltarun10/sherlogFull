// API Configuration
// Change this URL when deploying to production

const config = {
  // Backend API base URL
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  
  // API endpoints
  endpoints: {
    chat: '/api/chat',
    health: '/api/health'
  }
};

export default config;
