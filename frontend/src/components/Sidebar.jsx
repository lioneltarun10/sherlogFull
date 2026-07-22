import zeissLogo from '../assets/Zeiss-Logo-Login.svg'

function Sidebar({ 
  conversations, 
  activeConversationId, 
  onSelectConversation, 
  onNewChat, 
  onDeleteConversation,
  isOpen,
  onClose 
}) {
  return (
    <>
      {/* Overlay for mobile */}
      {isOpen && (
        <div 
          className="md:hidden fixed inset-0 bg-black/50 z-30"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside className={`
        fixed md:relative z-40 h-full w-64 bg-gray-800 flex flex-col
        transform transition-transform duration-300 ease-in-out
        ${isOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}
      `}>
        {/* Logo/Title Area */}
        <div className="p-4 border-b border-gray-700">
          <div className="flex items-center gap-3">
            <img src={zeissLogo} alt="ZEISS Logo" className="h-8 w-auto" />
            <span className="text-lg font-semibold">SherLogs</span>
          </div>
        </div>

        {/* New Chat Button */}
        <div className="p-3">
          <button
            onClick={onNewChat}
            className="w-full flex items-center gap-3 px-4 py-3 rounded-lg border border-gray-600 hover:bg-gray-700 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            <span>New Chat</span>
          </button>
        </div>

        {/* Chat History List */}
        <div className="flex-1 overflow-y-auto p-3 space-y-1">
          <p className="text-xs text-gray-500 uppercase tracking-wider px-3 mb-2">Recent Chats</p>
          {conversations.map((conversation) => (
            <div
              key={conversation.id}
              className={`
                group flex items-center gap-3 px-3 py-2 rounded-lg cursor-pointer transition-colors
                ${activeConversationId === conversation.id 
                  ? 'bg-gray-700' 
                  : 'hover:bg-gray-700/50'}
              `}
              onClick={() => onSelectConversation(conversation.id)}
            >
              <svg className="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
              <span className="flex-1 truncate text-sm">{conversation.title}</span>
              <button
                onClick={(e) => {
                  e.stopPropagation()
                  onDeleteConversation(conversation.id)
                }}
                className="opacity-0 group-hover:opacity-100 p-1 hover:bg-gray-600 rounded transition-all"
              >
                <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-700">
          <div className="flex items-center gap-3 text-sm text-gray-400">
            <div className="w-8 h-8 bg-gray-600 rounded-full flex items-center justify-center">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <span>Guest User</span>
          </div>
        </div>
      </aside>
    </>
  )
}

export default Sidebar
