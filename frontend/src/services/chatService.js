import config from '../config/api.config';

/**
 * Chat API Service
 * Handles all communication with the backend chat API
 */

class ChatApiError extends Error {
  constructor(message, status, data = null) {
    super(message);
    this.name = 'ChatApiError';
    this.status = status;
    this.data = data;
  }
}

/**
 * Send a message to the chat API and get a response
 * @param {string} message - The user's message
 * @returns {Promise<{reply: string}>} - The assistant's response
 * @throws {ChatApiError} - If the request fails
 */
export async function sendMessage(message) {
  const url = `${config.apiBaseUrl}${config.endpoints.chat}`;
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ message }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new ChatApiError(
        errorData?.reply || `Request failed with status ${response.status}`,
        response.status,
        errorData
      );
    }

    const data = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ChatApiError) {
      throw error;
    }
    
    // Network error or other fetch error
    throw new ChatApiError(
      'Unable to connect to the server. Please check your connection and try again.',
      0,
      null
    );
  }
}

/**
 * Check if the backend API is healthy
 * @returns {Promise<boolean>} - True if the API is reachable
 */
export async function checkHealth() {
  const url = `${config.apiBaseUrl}${config.endpoints.health}`;
  
  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });
    
    return response.ok;
  } catch (error) {
    return false;
  }
}

/**
 * Analyze logs using traceId and a user prompt
 * @param {string} traceId - The trace ID to search for in logs
 * @param {string} userPrompt - The user's question about the logs
 * @returns {Promise<{traceId: string, serviceName: string, logs: string[], aiResponse: string, success: boolean, errorMessage: string|null}>}
 * @throws {ChatApiError} - If the request fails
 */
export async function analyzeLogs(traceId, userPrompt) {
  const url = `${config.apiBaseUrl}${config.endpoints.analyze}`;
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ traceId, userPrompt }),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new ChatApiError(
        data?.errorMessage || `Request failed with status ${response.status}`,
        response.status,
        data
      );
    }

    return data;
  } catch (error) {
    if (error instanceof ChatApiError) {
      throw error;
    }
    
    // Network error or other fetch error
    throw new ChatApiError(
      'Unable to connect to the server. Please check your connection and try again.',
      0,
      null
    );
  }
}

export { ChatApiError };
