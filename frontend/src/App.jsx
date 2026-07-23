import { useState, useRef, useEffect } from 'react'
import Sidebar from './components/Sidebar'
import ChatArea from './components/ChatArea'
import InputBox from './components/InputBox'
import zeissLogo from './assets/Zeiss-Logo-Login.svg'
import { analyzeLogs } from './services/chatService'

function App() {
  const [conversations, setConversations] = useState([
    { id: 1, title: 'New Chat', messages: [] }
  ])
  const [activeConversationId, setActiveConversationId] = useState(1)
  const [isLoading, setIsLoading] = useState(false)
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const nextConversationId = useRef(2)

  const activeConversation = conversations.find(c => c.id === activeConversationId)

  const handleSendMessage = async ({ traceId, userPrompt }) => {
    if (!traceId.trim() || !userPrompt.trim() || isLoading) return

    // Add user message showing both traceId and question
    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: `**Trace ID:** ${traceId}\n\n${userPrompt}`
    }

    setConversations(prev => prev.map(conv => {
      if (conv.id === activeConversationId) {
        const newTitle = conv.messages.length === 0 
          ? `Trace: ${traceId.slice(0, 20)}...`
          : conv.title
        return {
          ...conv,
          title: newTitle,
          messages: [...conv.messages, userMessage]
        }
      }
      return conv
    }))

    // Send message to backend API
    setIsLoading(true)
    
    try {
      const response = await analyzeLogs(traceId, userPrompt)
      
      // Build response content showing logs and AI analysis
      let responseContent = ''
      
      if (response.logs && response.logs.length > 0) {
        responseContent += `**Found ${response.logs.length} log entries:**\n\n`
        responseContent += '```\n' + response.logs.slice(0, 10).join('\n') + '\n```\n\n'
        if (response.logs.length > 10) {
          responseContent += `*(Showing first 10 of ${response.logs.length} logs)*\n\n`
        }
      }
      
      responseContent += '**Analysis:**\n\n' + response.aiResponse

      const assistantMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: responseContent
      }

      setConversations(prev => prev.map(conv => {
        if (conv.id === activeConversationId) {
          return {
            ...conv,
            messages: [...conv.messages, assistantMessage]
          }
        }
        return conv
      }))
    } catch (error) {
      // Show error message in the chat
      const errorMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: `⚠️ Error: ${error.message || 'Something went wrong. Please try again.'}`
      }

      setConversations(prev => prev.map(conv => {
        if (conv.id === activeConversationId) {
          return {
            ...conv,
            messages: [...conv.messages, errorMessage]
          }
        }
        return conv
      }))
    } finally {
      setIsLoading(false)
    }
  }

  const handleNewChat = () => {
    const newConversation = {
      id: nextConversationId.current,
      title: 'New Chat',
      messages: []
    }
    nextConversationId.current++
    setConversations(prev => [newConversation, ...prev])
    setActiveConversationId(newConversation.id)
  }

  const handleSelectConversation = (id) => {
    setActiveConversationId(id)
  }

  const handleDeleteConversation = (id) => {
    if (conversations.length === 1) {
      // If it's the last conversation, just clear it
      setConversations([{ id: 1, title: 'New Chat', messages: [] }])
      setActiveConversationId(1)
      nextConversationId.current = 2
    } else {
      setConversations(prev => prev.filter(c => c.id !== id))
      if (activeConversationId === id) {
        const remaining = conversations.filter(c => c.id !== id)
        setActiveConversationId(remaining[0]?.id)
      }
    }
  }

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden">
      {/* Mobile sidebar toggle */}
      <button
        onClick={() => setSidebarOpen(!sidebarOpen)}
        className="md:hidden fixed top-3 left-3 z-50 p-2.5 bg-slate-800 border border-slate-700 rounded-xl hover:bg-slate-700 transition-colors shadow-lg"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      {/* Sidebar */}
      <Sidebar
        conversations={conversations}
        activeConversationId={activeConversationId}
        onSelectConversation={handleSelectConversation}
        onNewChat={handleNewChat}
        onDeleteConversation={handleDeleteConversation}
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Header */}
        <header className="flex items-center justify-center gap-3 h-14 border-b border-slate-800 bg-slate-900/80 backdrop-blur-sm flex-shrink-0">
          <img src={zeissLogo} alt="ZEISS Logo" className="h-6 w-auto" />
          <h1 className="text-lg font-semibold bg-gradient-to-r from-slate-100 to-slate-300 bg-clip-text text-transparent">SherLog</h1>
        </header>

        {/* Messages */}
        <ChatArea 
          messages={activeConversation?.messages || []} 
          isLoading={isLoading}
        />

        {/* Input */}
        <InputBox 
          onSendMessage={handleSendMessage} 
          disabled={isLoading}
        />
      </div>
    </div>
  )
}

export default App
