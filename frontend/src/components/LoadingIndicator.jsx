function LoadingIndicator() {
  return (
    <div className="flex gap-3 mb-4">
      {/* Avatar */}
      <div className="w-9 h-9 rounded-xl flex-shrink-0 flex items-center justify-center bg-gradient-to-br from-emerald-500 to-teal-600 shadow-lg pulse-subtle">
        <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
      </div>

      {/* Typing indicator */}
      <div className="bg-slate-800/80 border border-slate-700/50 px-5 py-4 rounded-2xl rounded-tl-md shadow-md">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1.5">
            <span className="typing-dot w-2 h-2 bg-emerald-400 rounded-full"></span>
            <span className="typing-dot w-2 h-2 bg-emerald-400 rounded-full"></span>
            <span className="typing-dot w-2 h-2 bg-emerald-400 rounded-full"></span>
          </div>
          <span className="text-sm text-slate-400 ml-1">Analyzing logs...</span>
        </div>
      </div>
    </div>
  )
}

export default LoadingIndicator
