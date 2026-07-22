function MessageBubble({ message }) {
  const isUser = message.role === 'user'

  return (
    <div className={`flex gap-4 mb-6 ${isUser ? 'flex-row-reverse' : ''}`}>
      {/* Avatar */}
      <div className={`
        w-8 h-8 rounded-full flex-shrink-0 flex items-center justify-center
        ${isUser 
          ? 'bg-blue-600' 
          : 'bg-gradient-to-br from-green-500 to-teal-600'}
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
        flex-1 max-w-[80%]
        ${isUser ? 'flex justify-end' : ''}
      `}>
        <div className={`
          px-4 py-3 rounded-2xl
          ${isUser 
            ? 'bg-blue-600 text-white rounded-br-md' 
            : 'bg-gray-700 text-gray-100 rounded-bl-md'}
        `}>
          <p className="whitespace-pre-wrap leading-relaxed">{message.content}</p>
        </div>
      </div>
    </div>
  )
}

export default MessageBubble
