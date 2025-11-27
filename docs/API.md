# BuzzLink API Documentation

Base URL: `http://localhost:8080`

## Authentication

All API requests require the `X-Clerk-User-Id` header containing the authenticated user's Clerk ID.

```
X-Clerk-User-Id: user_2abc123def456
```

## REST API Endpoints

### Channels

#### List All Channels

```
GET /api/channels
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "general",
    "description": "General discussion",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

#### Get Channel by ID

```
GET /api/channels/{id}
```

**Response:**
```json
{
  "id": 1,
  "name": "general",
  "description": "General discussion",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Create Channel

```
POST /api/channels
Content-Type: application/json

{
  "name": "engineering",
  "description": "Engineering team discussions"
}
```

### Messages

#### Get Channel Messages

```
GET /api/channels/{channelId}/messages?limit=50
```

**Query Parameters:**
- `limit` (optional, default: 50) - Number of recent messages to return

**Response:**
```json
[
  {
    "id": 123,
    "channelId": 1,
    "sender": {
      "id": 1,
      "clerkId": "user_2abc123",
      "displayName": "John Doe",
      "avatarUrl": "https://...",
      "isAdmin": false
    },
    "content": "Hello everyone!",
    "type": "TEXT",
    "createdAt": "2024-01-15T14:30:00",
    "reactionCount": 3
  }
]
```

#### Delete Message (Admin Only)

```
DELETE /api/messages/{messageId}
X-Clerk-User-Id: user_2abc123
```

**Response:** `204 No Content` on success, `403 Forbidden` if not admin

#### Toggle Reaction

```
POST /api/messages/{messageId}/reactions
X-Clerk-User-Id: user_2abc123
```

**Response:**
```json
{
  "count": 4
}
```

### Users

#### Sync User

Called automatically on login to create or update user in database.

```
POST /api/users/sync
Content-Type: application/json

{
  "clerkId": "user_2abc123",
  "displayName": "John Doe",
  "email": "john@example.com",
  "avatarUrl": "https://..."
}
```

**Response:**
```json
{
  "id": 1,
  "clerkId": "user_2abc123",
  "displayName": "John Doe",
  "avatarUrl": "https://...",
  "isAdmin": false,
  "email": "john@example.com"
}
```

#### Get Current User

```
GET /api/users/me
X-Clerk-User-Id: user_2abc123
```

#### Update Profile

```
PUT /api/users/me
X-Clerk-User-Id: user_2abc123
Content-Type: application/json

{
  "displayName": "John Smith",
  "avatarUrl": "https://..."
}
```

#### Make User Admin (Demo Only)

```
POST /api/users/make-admin
Content-Type: application/json

{
  "clerkId": "user_2abc123",
  "isAdmin": true
}
```

## WebSocket API

### Connection

Connect to: `ws://localhost:8080/ws` using SockJS + STOMP

**JavaScript Example:**
```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8080/ws');
const client = new Client({
  webSocketFactory: () => socket,
  reconnectDelay: 5000
});

client.onConnect = () => {
  console.log('Connected');
};

client.activate();
```

### Subscribe to Topics

#### Channel Messages

Subscribe to receive new messages in a channel:

```javascript
client.subscribe('/topic/channel.1', (message) => {
  const chatMessage = JSON.parse(message.body);
  console.log('New message:', chatMessage);
});
```

**Message Format:**
```json
{
  "id": 123,
  "channelId": 1,
  "sender": { ... },
  "content": "Hello!",
  "type": "TEXT",
  "createdAt": "2024-01-15T14:30:00",
  "reactionCount": 0
}
```

#### Typing Indicators

Subscribe to typing events:

```javascript
client.subscribe('/topic/channel.1.typing', (message) => {
  const event = JSON.parse(message.body);
  console.log('Typing:', event);
});
```

**Event Format:**
```json
{
  "channelId": 1,
  "clerkId": "user_2abc123",
  "displayName": "John Doe",
  "isTyping": true
}
```

#### Presence Updates

Subscribe to online user updates:

```javascript
client.subscribe('/topic/channel.1.presence', (message) => {
  const event = JSON.parse(message.body);
  console.log('Online users:', event.onlineCount);
});
```

**Event Format:**
```json
{
  "channelId": 1,
  "onlineUsers": ["user_1", "user_2"],
  "onlineCount": 2
}
```

### Send Messages

#### Send Chat Message

```javascript
client.publish({
  destination: '/app/chat.sendMessage',
  body: JSON.stringify({
    channelId: 1,
    clerkId: 'user_2abc123',
    content: 'Hello everyone!',
    type: 'TEXT'
  })
});
```

#### Send Typing Indicator

```javascript
client.publish({
  destination: '/app/chat.typing',
  body: JSON.stringify({
    channelId: 1,
    clerkId: 'user_2abc123',
    displayName: 'John Doe',
    isTyping: true
  })
});
```

#### Join Channel

```javascript
client.publish({
  destination: '/app/chat.join',
  body: JSON.stringify({
    channelId: 1,
    clerkId: 'user_2abc123'
  })
});
```

#### Leave Channel

```javascript
client.publish({
  destination: '/app/chat.leave',
  body: JSON.stringify({
    channelId: 1,
    clerkId: 'user_2abc123'
  })
});
```

## Error Responses

All endpoints may return error responses:

**400 Bad Request:**
```json
{
  "error": "Invalid request",
  "message": "Channel name is required"
}
```

**403 Forbidden:**
```json
{
  "error": "Forbidden",
  "message": "Only admins can delete messages"
}
```

**404 Not Found:**
```json
{
  "error": "Not found",
  "message": "Channel not found"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

## Rate Limiting

In production, the following rate limits would apply:
- REST API: 100 requests per minute per user
- WebSocket messages: 60 messages per minute per connection
- Typing events: 10 per minute per user

## CORS

The backend allows CORS requests from `http://localhost:3000` in development.

In production, this should be configured to allow requests only from the deployed frontend domain.
