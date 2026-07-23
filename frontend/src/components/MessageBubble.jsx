function MessageBubble({ message }) {
  const isUser = message.role === 'user'
  const isError = message.content.startsWith('⚠️')

  // Parse content to render sections with proper formatting
  const renderContent = (content) => {
    // Split by code blocks to render them properly
    const parts = content.split(/(```[\s\S]*?```)/g)
    
    return parts.map((part, index) => {
      if (part.startsWith('```') && part.endsWith('```')) {
        // Code block
        const codeContent = part.slice(3, -3).replace(/^\w*\n/, '') // Remove language hint
        return (
          <pre key={index} className="bg-slate-900 border border-slate-700 rounded-lg p-3 my-3 overflow-x-auto">
            <code className="text-sm text-slate-300 whitespace-pre-wrap break-all font-mono">
              {codeContent}
            </code>
          </pre>
        )
      }
      
      // Regular text - convert **text** to bold spans
      const textParts = part.split(/(\*\*[^*]+\*\*)/g)
      return (
        <span key={index}>
          {textParts.map((textPart, textIndex) => {
            if (textPart.startsWith('**') && textPart.endsWith('**')) {
              const boldText = textPart.slice(2, -2)
              // Check if it's a section header
              if (boldText.includes('log entries') || boldText === 'Analysis:' || boldText.startsWith('Found') || boldText.startsWith('Trace ID:')) {
                return (
                  <span key={textIndex} className="block mt-4 mb-2 first:mt-0">
                    <span className="inline-flex items-center gap-2 text-sky-400 font-semibold text-sm uppercase tracking-wide">
                      {boldText.includes('log') && (
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                      )}
                      {boldText === 'Analysis:' && (
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                        </svg>
                      )}
                      {boldText.startsWith('Trace') && (
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14" />
                        </svg>
                      )}
                      {boldText}
                    </span>
                  </span>
                )
              }
              return <strong key={textIndex} className="text-sky-300 font-semibold">{boldText}</strong>
            }
            return textPart
          })}
        </span>
      )
    })
  }

  return (
    <div className={`flex gap-3 mb-4 ${isUser ? 'flex-row-reverse' : ''}`}>
      {/* Avatar */}
      <div className={`
        w-9 h-9 rounded-xl flex-shrink-0 flex items-center justify-center shadow-lg
        ${isUser 
          ? 'bg-gradient-to-br from-blue-500 to-blue-600' 
          : 'bg-gradient-to-br from-emerald-500 to-teal-600'}
      `}>
        {isUser ? (
          <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        ) : (
          <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
        )}
      </div>

      {/* Message content */}
      <div className={`
        flex-1 min-w-0 max-w-[85%]
        ${isUser ? 'flex justify-end' : ''}
      `}>
        <div className={`
          px-4 py-3 rounded-2xl shadow-md
          ${isUser 
            ? 'bg-gradient-to-br from-blue-600 to-blue-700 text-white rounded-tr-md' 
            : isError
              ? 'bg-slate-800/80 border border-red-500/30 text-slate-100 rounded-tl-md'
              : 'bg-slate-800/80 border border-slate-700/50 text-slate-100 rounded-tl-md'}
        `}>
          {isError ? (
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-red-500/20 rounded-full flex items-center justify-center mt-0.5">
                <svg className="w-4 h-4 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-red-400 font-medium text-sm mb-1">Something went wrong</p>
                <p className="text-slate-300 text-sm leading-relaxed break-words">
                  {message.content.replace('⚠️ Error:', '').trim()}
                </p>
              </div>
            </div>
          ) : (
            <div className="message-content text-sm leading-relaxed">
              {renderContent(message.content)}
            </div>
          )}
        </div>
        
        {/* Timestamp */}
        <div className={`text-xs text-slate-500 mt-1 ${isUser ? 'text-right mr-1' : 'ml-1'}`}>
          {new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </div>
      </div>
    </div>
  )
}

export default MessageBubble
