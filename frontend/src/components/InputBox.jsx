import { useState } from 'react'

function InputBox({ onSendMessage, disabled }) {
  const [traceId, setTraceId] = useState('')
  const [input, setInput] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    if (input.trim() && !disabled) {
      // traceId is optional - send it if present, otherwise null/empty
      onSendMessage({ 
        traceId: traceId.trim() || null, 
        userPrompt: input.trim() 
      })
      setInput('')
      // Keep traceId for convenience - user might want to ask multiple questions about same trace
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSubmit(e)
    }
  }

  // traceId is now optional - only userPrompt is required
  const canSubmit = input.trim() && !disabled

  return (
    <div className="border-t border-slate-700/50 bg-gradient-to-t from-slate-900 to-slate-900/95 px-4 py-4">
      <form onSubmit={handleSubmit} className="max-w-3xl mx-auto space-y-3">
        {/* Trace ID Input */}
        <div className="flex items-center gap-2 bg-slate-800/60 border border-slate-700/50 rounded-xl px-3 py-2 focus-within:border-sky-500/50 focus-within:ring-1 focus-within:ring-sky-500/20 transition-all">
          <div className="flex items-center gap-2 text-slate-400">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14" />
            </svg>
            <span className="text-sm font-medium whitespace-nowrap">Trace ID</span>
          </div>
          <div className="w-px h-5 bg-slate-600"></div>
          <input
            type="text"
            value={traceId}
            onChange={(e) => setTraceId(e.target.value)}
            placeholder="Optional: Enter trace ID to search logs..."
            disabled={disabled}
            className="flex-1 bg-transparent border-none outline-none px-2 py-1 text-slate-100 placeholder-slate-500 text-sm"
          />
          {traceId && (
            <button
              type="button"
              onClick={() => setTraceId('')}
              className="p-1 hover:bg-slate-700 rounded-md transition-colors"
            >
              <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>

        {/* User Prompt Input */}
        <div className="relative flex items-end gap-2 bg-slate-800/60 border border-slate-700/50 rounded-2xl px-3 py-2 focus-within:border-sky-500/50 focus-within:ring-1 focus-within:ring-sky-500/20 transition-all">
          <div className="flex-shrink-0 pt-2 text-slate-400">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask a question about the logs..."
            disabled={disabled}
            rows={1}
            className="flex-1 bg-transparent border-none outline-none resize-none px-2 py-2 text-slate-100 placeholder-slate-500 max-h-32 overflow-y-auto text-sm leading-relaxed"
            style={{ minHeight: '40px' }}
          />
          <button
            type="submit"
            disabled={!canSubmit}
            className={`
              flex-shrink-0 p-2.5 rounded-xl transition-all duration-200 shadow-md
              ${canSubmit
                ? 'bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 text-white shadow-sky-500/25 hover:shadow-sky-500/40' 
                : 'bg-slate-700 text-slate-500 cursor-not-allowed shadow-none'}
            `}
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </button>
        </div>

        {/* Helper text */}
        <div className="flex items-center justify-center gap-4 text-xs text-slate-500">
          <span className="flex items-center gap-1">
            <kbd className="px-1.5 py-0.5 bg-slate-800 rounded text-slate-400 font-mono text-[10px]">Enter</kbd>
            <span>to send</span>
          </span>
          <span className="flex items-center gap-1">
            <kbd className="px-1.5 py-0.5 bg-slate-800 rounded text-slate-400 font-mono text-[10px]">Shift + Enter</kbd>
            <span>for new line</span>
          </span>
        </div>
      </form>
    </div>
  )
}

export default InputBox
