# Testing the MDM Bot Without AI

## Current Status

The bot is currently configured but the Gemini API key is not set. Here's what's happening:

### ✅ **Fixed Issues:**

1. **Dashboard Layout**: The MDM Bot section now has its own scrollbar and is contained in a single screen
2. **Error Handling**: The bot now provides clear error messages when the API key is not configured
3. **Service Communication**: All services are running and communicating properly

### 🔧 **Current Bot Response:**

When you ask "Hi Can you give me merge candidates?", the bot currently responds with:
```
I'm sorry, but my AI capabilities are not configured. Please ask the administrator to configure the Gemini API key in the application.properties file. For now, I can help you with basic commands like 'show candidates' or 'merge all'.
```

## How to Configure the Bot

### Step 1: Get a Gemini API Key
1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated API key

### Step 2: Configure the API Key
Edit the file: `mdm-ai-orchestration/src/main/resources/application.properties`

Replace this line:
```properties
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
```

With your actual API key:
```properties
gemini.api.key=your_actual_api_key_here
```

### Step 3: Restart the AI Orchestration Service
```bash
# Stop the current service (Ctrl+C in the terminal)
# Then restart it:
cd mdm-ai-orchestration
mvn spring-boot:run
```

## Testing the Bot

### Test 1: Without API Key (Current State)
```bash
curl -X POST http://localhost:8083/api/bot/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hi Can you give me merge candidates?"}'
```

**Expected Response:**
```json
{
  "response": "I'm sorry, but my AI capabilities are not configured. Please ask the administrator to configure the Gemini API key in the application.properties file. For now, I can help you with basic commands like 'show candidates' or 'merge all'."
}
```

### Test 2: With API Key (After Configuration)
After configuring the API key, the bot should respond intelligently to natural language queries like:

- "Show me merge candidates"
- "What needs review?"
- "Merge all pending"
- "Preview merge 123"
- "Show audit logs for 456"

## Dashboard Features

### ✅ **Working Features:**
1. **Single Screen Layout**: Both dashboard and bot chat are visible simultaneously
2. **Independent Scrollbars**: Each section has its own scrollbar
3. **Real-time Chat**: Messages are sent and received in real-time
4. **Error Handling**: Clear error messages when services are unavailable
5. **Responsive Design**: Works well on different screen sizes

### 🎯 **Dashboard Layout Improvements:**
- **Full Viewport Height**: Uses 100vh to maximize screen space
- **Fixed Chat Area**: Chat box has a maximum height with scrollbar
- **Dashboard Scroll**: Main content area scrolls independently
- **No Body Scroll**: Prevents page-level scrolling for better UX

## Next Steps

1. **Configure API Key**: Add your Gemini API key to enable AI functionality
2. **Test Natural Language**: Try asking the bot questions in natural language
3. **Load Test Data**: Add some merge candidates to see the full workflow
4. **Explore Features**: Test all bot capabilities (show, preview, merge, audit)

## Troubleshooting

### If the bot still shows errors after configuring the API key:
1. Check that the API key is valid and active
2. Ensure the AI Orchestration service was restarted
3. Verify the API key format (should be a long string without spaces)
4. Check the service logs for detailed error messages

### If the dashboard layout doesn't look right:
1. Clear your browser cache
2. Refresh the page
3. Check browser console for any JavaScript errors
4. Ensure all CSS is loading properly 