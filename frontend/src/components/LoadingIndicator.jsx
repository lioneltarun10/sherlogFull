function LoadingIndicator() {
  return (
    <div className="flex gap-4 mb-6">
      {/* Avatar */}
      <div className="w-8 h-8 rounded-full flex-shrink-0 flex items-center justify-center bg-gradient-to-br from-green-500 to-teal-600">
        <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
      </div>

      {/* Typing indicator */}
      <div className="bg-gray-700 px-4 py-3 rounded-2xl rounded-bl-md">
        <div className="flex items-center gap-1">
          <span className="typing-dot w-2 h-2 bg-gray-400 rounded-full"></span>
          <span className="typing-dot w-2 h-2 bg-gray-400 rounded-full"></span>
          <span className="typing-dot w-2 h-2 bg-gray-400 rounded-full"></span>
        </div>
      </div>
    </div>
  )
}

export default LoadingIndicator
