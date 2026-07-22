import { useEffect, useRef } from 'react'
import MessageBubble from './MessageBubble'
import LoadingIndicator from './LoadingIndicator'
import zeissLogo from '../assets/Zeiss-Logo-Login.svg'

function ChatArea({ messages, isLoading }) {
  const messagesEndRef = useRef(null)

  // Auto-scroll to latest message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, isLoading])

  return (
    <div className="flex-1 overflow-y-auto">
      {messages.length === 0 ? (
        // Empty state
        <div className="h-full flex flex-col items-center justify-center text-gray-400 p-8">
          <img src={zeissLogo} alt="ZEISS Logo" className="h-16 w-auto mb-6" />
          <h2 className="text-2xl font-semibold text-gray-200 mb-2">Welcome to SherLogs</h2>
          <p className="text-center max-w-md">
            Your AI-powered log analysis assistant. Ask me about your logs, troubleshooting, or system insights.
          </p>
        </div>
      ) : (
        // Messages list
        <div className="max-w-3xl mx-auto py-6 px-4">
          {messages.map((message) => (
            <MessageBubble key={message.id} message={message} />
          ))}
          {isLoading && <LoadingIndicator />}
          <div ref={messagesEndRef} />
        </div>
      )}
    </div>
  )
}

export default ChatArea
