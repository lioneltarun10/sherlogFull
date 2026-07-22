# SherLogs 🔍

**By The XORcists Team**

*Making logs tell their story—because troubleshooting shouldn't feel like detective work.*

---

## 🎯 The Problem

You're staring at Grafana. Logs are flooding in from dozens of microservices. Something's broken in production.

But here's the thing: **Grafana visualizes. It doesn't understand.**

It doesn't know:
- What your services actually *do*
- How they talk to each other
- What "normal" looks like for your workflow
- The story behind those cryptic error messages

Your Ops team, Application Managers, and Developers are left connecting the dots manually—cross-referencing wikis, reading service docs, piecing together timelines, and making educated guesses.

**There has to be a better way.**

---

## 💡 Our Solution

**SherLogs** is an AI-powered troubleshooting assistant that transforms your logs from raw data into actionable insights.

### How It Works

1. **Connect**: Plug into your existing Grafana/OTEL-LGTM stack
2. **Contextualize**: Combine logs with service documentation, wiki pages, and knowledge bases
3. **Analyze**: LLM understands microservice architectures, API flows, and workflows
4. **Deliver**: Get context-aware stories, timeline correlations, conclusions, and actionable suggestions

### What Makes It Different

Unlike traditional log visualization tools, SherLogs:
- ✅ **Thinks like a developer** — understands service relationships and dependencies
- ✅ **Draws connections** — correlates events across multiple services with timeline awareness
- ✅ **Provides context** — references your actual service documentation and workflows
- ✅ **Suggests solutions** — offers assumptions, conclusions, and next steps based on patterns

---

## 🎪 Who Benefits

- **Ops Teams**: First responders get intelligent triage and faster root cause identification
- **Application Managers**: Better visibility into system health with narrative explanations
- **Developers**: Quick context when debugging production issues
- **Product Owners**: Understand impact without diving into technical logs

---

## 📁 Project Structure

```
SherLog/
├── frontend/           # React + Vite frontend
│   ├── src/
│   │   ├── components/
│   │   │   ├── ChatArea.jsx
│   │   │   ├── InputBox.jsx
│   │   │   ├── LoadingIndicator.jsx
│   │   │   ├── MessageBubble.jsx
│   │   │   └── Sidebar.jsx
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   ├── public/
│   ├── index.html
│   └── package.json
├── backend/            # Backend service
│   ├── src/
│   │   └── index.js
│   └── package.json
├── .gitignore
└── README.md
```

## 🚀 Getting Started

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 in your browser.

### Backend

```bash
cd backend
npm install
npm run dev
```

---

## 🏗️ Architecture (High-Level)

```
[Grafana/OTEL-LGTM] → [Log Aggregation]
                              ↓
                    [SherLogs AI Engine]
                              ↓
        [Service Docs (MD) + Wiki + Knowledge Base]
                              ↓
                    [Contextual Analysis]
                              ↓
              [Insights + Suggestions + Timeline]
```

---

## 🚀 The Vision

Today: A troubleshooting assistant that reads your logs and documentation.

Tomorrow: A proactive system that learns your patterns, predicts issues, and prevents fires before they start.

---

## 🛠️ Built During

**Innovate@2030 Hackathon**  
July 22-23, 2026

---

## 👥 The XORcists Team

- Krishnan, Sai
- Goel, Shreya
- Unnam, Tarun
- Tapse, Harshitha

*Debugging the future, one log at a time.* 🕵️

---

**Status**: Hackathon Project 🏗️  
**Stack**: Grafana, OTEL-LGTM, LLM Integration, Knowledge Base  
**Goal**: Make troubleshooting feel less like archaeology, more like insight.