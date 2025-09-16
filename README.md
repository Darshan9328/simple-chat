# Simple Chat Application

A real-time chat application built with Spring Boot, WebSocket, and JWT authentication.

## Features

- Real-time messaging using WebSocket
- JWT-based authentication
- Private messaging between users
- Message persistence in PostgreSQL database
- Modern, responsive UI
- Typing indicators
- Message status tracking

## How to Run

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Access the application:**
   - Open your browser and go to: `http://localhost:8080`
   - The application will serve the chat interface

3. **Test the chat:**
   - Register a new user or login with existing credentials
   - Open multiple browser tabs/windows to simulate different users
   - Login with different usernames in each tab
   - Start chatting between users

## Testing the WebSocket Functionality

### Issue Fixed:
- ✅ **Real-time message delivery**: Messages now appear instantly on both sender and receiver sides
- ✅ **Sender message display**: Sent messages now show immediately in the sender's chat
- ✅ **No page refresh needed**: Messages appear in real-time without refreshing

### How to Test:

1. **Open two browser tabs/windows**
2. **Tab 1**: Register/Login as "user1"
3. **Tab 2**: Register/Login as "user2"
4. **In Tab 1**: Select "user2" from the user list
5. **Send a message from Tab 1**
6. **Verify**: 
   - Message appears immediately in Tab 1 (sender)
   - Message appears immediately in Tab 2 (receiver)
   - No page refresh required

## API Endpoints

- `POST /api/register` - Register a new user
- `POST /api/login` - Login and get JWT token
- `GET /api/users` - Get list of available users
- `GET /api/conversations` - Get user's conversations
- `GET /api/conversations/{id}/messages` - Get conversation messages

## WebSocket Endpoints

- `/ws` - WebSocket connection endpoint
- `/app/chat.sendPrivateMessage` - Send private message
- `/app/chat.addUser` - Add user to session
- `/app/chat.typing` - Send typing indicator
- `/app/chat.markAsRead` - Mark messages as read

## Database

The application uses PostgreSQL. Make sure to:
1. Install PostgreSQL
2. Create a database named `simple_chat`
3. Update `application.properties` with your database credentials

## Technologies Used

- Spring Boot 3.5.3
- Spring WebSocket
- Spring Security with JWT
- PostgreSQL
- HTML5, CSS3, JavaScript
- SockJS and STOMP for WebSocket communication
- Bootstrap for UI

## Troubleshooting

If messages are not appearing in real-time:

1. Check browser console for WebSocket connection errors
2. Verify the application is running on port 8080
3. Ensure no firewall is blocking WebSocket connections
4. Check that both users are properly logged in and connected

## Recent Fixes

- Fixed WebSocket message routing to ensure messages reach both sender and receiver
- Added proper message broadcasting in WebSocketChatController
- Created complete frontend with real-time message handling
- Added conversation history loading
- Implemented typing indicators
- Added connection status monitoring
