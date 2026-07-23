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
    <div className="flex-1 overflow-y-auto overflow-x-hidden bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950">
      {messages.length === 0 ? (
        // Empty state
        <div className="h-full flex flex-col items-center justify-center text-slate-400 p-8">
          <div className="relative mb-6">
            <div className="absolute inset-0 bg-sky-500/20 blur-3xl rounded-full"></div>
            <img src={zeissLogo} alt="ZEISS Logo" className="relative h-16 w-auto opacity-90" />
          </div>
          <h2 className="text-2xl font-semibold text-slate-100 mb-3">Welcome to SherLogs</h2>
          <p className="text-center max-w-md text-slate-400 leading-relaxed">
            Your AI-powered log analysis assistant. Enter a trace ID and ask questions to get insights about your logs.
          </p>
          
          {/* Quick tips */}
          <div className="mt-8 grid gap-3 max-w-lg w-full">
            <div className="flex items-start gap-3 bg-slate-800/40 border border-slate-700/30 rounded-xl p-4">
              <div className="w-8 h-8 bg-sky-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                <svg className="w-4 h-4 text-sky-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <div>
                <p className="text-sm font-medium text-slate-200">Search by Trace ID</p>
                <p className="text-xs text-slate-500 mt-0.5">Enter any trace ID to fetch related logs</p>
              </div>
            </div>
            <div className="flex items-start gap-3 bg-slate-800/40 border border-slate-700/30 rounded-xl p-4">
              <div className="w-8 h-8 bg-emerald-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                <svg className="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
              </div>
              <div>
                <p className="text-sm font-medium text-slate-200">AI Analysis</p>
                <p className="text-xs text-slate-500 mt-0.5">Ask questions and get intelligent insights</p>
              </div>
            </div>
          </div>
        </div>
      ) : (
        // Messages list
        <div className="max-w-4xl mx-auto py-6 px-4">
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
