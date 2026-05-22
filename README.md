<<<<<<< HEAD
# 🧭 Hari Navigation Chatbot

A **multi-platform navigation chatbot** built with Java & Spring Boot that helps users get turn-by-turn directions via **WhatsApp**, **Telegram**, and **Instagram**. Users simply say *hi*, share their location, and get a Google Maps link to their destination — all inside their favourite messaging app.

---

## ✨ Features

=======
# TelegramNavigationChatbot
# 🧭 Hari Navigation Chatbot
A **multi-platform navigation chatbot** built with Java & Spring Boot that helps users get turn-by-turn directions via **WhatsApp**, **Telegram**, and **Instagram**. Users simply say *hi*, share their location, and get a Google Maps link to their destination — all inside their favourite messaging app.
---
## ✨ Features
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
- 🤖 **Multi-platform support** — Works on WhatsApp (Meta Cloud API), Telegram Bot API, and Instagram
- 👋 **Conversational flow** — Greets the user first, then guides them step-by-step to their destination
- 📍 **Live location sharing** — Requests the user's current location using the platform's native location button
- 🗺️ **Google Maps integration** — Generates a direct Maps link for Driving, Walking, or Transit modes
- 📋 **Navigation history** — Stores and displays the user's past navigation sessions
- 🔄 **Session management** — Tracks each user's state (Welcome → Destination → Location → Navigating) using Firebase Firestore
- ☁️ **Cloud-ready** — Deployable to Render with a single `render.yaml` config
- 🧪 **Unit tested** — Core chatbot logic covered by JUnit 5 + Mockito tests
<<<<<<< HEAD

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| HTTP Client | OkHttp 4.12 |
| Database | Firebase Firestore (via Firebase Admin SDK 9.2) |
| Messaging APIs | Meta WhatsApp Cloud API, Telegram Bot API, Instagram Graph API |
| Maps | Google Maps Directions URL API |
| Build | Maven |
| Deployment | Render / ngrok (local tunnel) |
| Testing | JUnit 5, Mockito |

---

## 💬 Conversation Flow

=======
---
## 🛠️ Tech Stack
|
 Layer 
|
 Technology 
|
|
---
|
---
|
|
 Language 
|
 Java 17 
|
|
 Framework 
|
 Spring Boot 3.3.5 
|
|
 HTTP Client 
|
 OkHttp 4.12 
|
|
 Database 
|
 Firebase Firestore (via Firebase Admin SDK 9.2) 
|
|
 Messaging APIs 
|
 Meta WhatsApp Cloud API, Telegram Bot API, Instagram Graph API 
|
|
 Maps 
|
 Google Maps Directions URL API 
|
|
 Build 
|
 Maven 
|
|
 Deployment 
|
 Render / ngrok (local tunnel) 
|
|
 Testing 
|
 JUnit 5, Mockito 
|
---
## 💬 Conversation Flow
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
```
User:  hi
Bot:   Hello Hari! 👋
       Welcome to the Navigation Chatbot!
<<<<<<< HEAD

Bot:   🗺️ Where would you like to go? Type your destination.

User:  Railway Station

Bot:   🎯 Destination set: Railway Station
       Now please share your current location.
       [📍 Share Location button]

User:  <shares location>

Bot:   How would you like to travel?
       [🚗 Drive]  [🚶 Walk]  [🚌 Transit]

User:  Drive

=======
Bot:   🗺️ Where would you like to go? Type your destination.
User:  Railway Station
Bot:   🎯 Destination set: Railway Station
       Now please share your current location.
       [📍 Share Location button]
User:  <shares location>
Bot:   How would you like to travel?
       [🚗 Drive]  [🚶 Walk]  [🚌 Transit]
User:  Drive
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
Bot:   🚗 Navigation Started!
       📍 Destination: Railway Station
       🛣️ Mode: DRIVING
       🔗 Open in Maps: https://maps.google.com/...
```
<<<<<<< HEAD

---

## 📁 Project Structure

=======
---
## 📁 Project Structure
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
```
WhatsappChatbot/
├── src/
│   ├── main/java/com/navigation/chatbot/
│   │   ├── ChatbotApplication.java        # Spring Boot entry point
│   │   ├── config/                        # App & async thread config
│   │   ├── controller/
│   │   │   ├── WebhookController.java     # Receives webhook events from all platforms
│   │   │   └── ApiController.java         # REST API for admin/health
│   │   ├── model/
│   │   │   ├── UserSession.java           # User session state machine
│   │   │   ├── NavigationRequest.java     # A single navigation trip record
│   │   │   ├── WhatsAppWebhookPayload.java
│   │   │   ├── TelegramWebhookPayload.java
│   │   │   └── InstagramWebhookPayload.java
│   │   ├── repository/
│   │   │   ├── UserSessionRepository.java
│   │   │   └── NavigationRequestRepository.java
│   │   └── service/
│   │       ├── ChatbotService.java         # Core conversation logic
│   │       ├── MessagingPlatform.java      # Common interface for all platforms
│   │       ├── WhatsAppMessageService.java # WhatsApp outbound messages
│   │       ├── TelegramMessageService.java # Telegram outbound messages
│   │       └── InstagramMessageService.java
│   └── test/
│       └── ChatbotServiceTest.java         # Unit tests
├── .env.example                            # Environment variable template
├── render.yaml                             # Render deployment config
├── pom.xml
└── README.md
```
<<<<<<< HEAD

---

## ⚙️ Setup & Configuration

### 1. Clone the repository

=======
---
## ⚙️ Setup & Configuration
### 1. Clone the repository
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
```bash
git clone https://github.com/your-username/WhatsappChatbot.git
cd WhatsappChatbot
```
<<<<<<< HEAD

### 2. Configure environment variables

Copy `.env.example` to `.env` and fill in your credentials:

=======
### 2. Configure environment variables
Copy `.env.example` to `.env` and fill in your credentials:
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
```env
META_PHONE_NUMBER_ID=your_phone_number_id_here
META_ACCESS_TOKEN=your_permanent_access_token_here
META_VERIFY_TOKEN=any_random_secret_string
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_CREDENTIALS_PATH=src/main/resources/firebase-service-account.json
PORT=8080
```
<<<<<<< HEAD

### 3. Add Firebase credentials

=======
### 3. Add Firebase credentials
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
Download your Firebase service account JSON from the Firebase Console and place it at:
```
src/main/resources/firebase-service-account.json
```
<<<<<<< HEAD

### 4. Run locally

```bash
mvn spring-boot:run
```

### 5. Expose locally with ngrok (for webhook testing)

```bash
ngrok http 8080
```

Copy the HTTPS URL and set it as your webhook in the Meta / Telegram developer dashboards.

---

## 🌐 Webhook Endpoints

| Platform | Endpoint |
|---|---|
| WhatsApp | `POST /webhook/whatsapp` |
| Telegram | `POST /webhook/telegram` |
| Instagram | `POST /webhook/instagram` |
| Health Check | `GET /actuator/health` |

---

## ☁️ Deploying to Render

=======
### 4. Run locally
```bash
mvn spring-boot:run
```
### 5. Expose locally with ngrok (for webhook testing)
```bash
ngrok http 8080
```
Copy the HTTPS URL and set it as your webhook in the Meta / Telegram developer dashboards.
---
## 🌐 Webhook Endpoints
|
 Platform 
|
 Endpoint 
|
|
---
|
---
|
|
 WhatsApp 
|
`POST /webhook/whatsapp`
|
|
 Telegram 
|
`POST /webhook/telegram`
|
|
 Instagram 
|
`POST /webhook/instagram`
|
|
 Health Check 
|
`GET /actuator/health`
|
---
## ☁️ Deploying to Render
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
1. Push your code to GitHub.
2. Create a new **Web Service** on [Render](https://render.com).
3. Set the environment variables in Render's dashboard.
4. For Firebase credentials on Render, use the `FIREBASE_CREDENTIALS_JSON` env var (paste the full JSON content).
5. Render uses `render.yaml` automatically:
<<<<<<< HEAD

=======
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
```yaml
build:
  command: mvn clean package -DskipTests
start:
  command: java -jar target/chatbot-0.0.1-SNAPSHOT.jar
```
<<<<<<< HEAD

---

## 🧪 Running Tests

```bash
mvn test
```

> **Note:** Tests require Java 17. If you are running Java 26, add `-Dnet.bytebuddy.experimental=true` as a JVM argument, or run with `--add-opens` flags to resolve Mockito/ByteBuddy compatibility.

---

## 📌 Useful Commands (in chat)

| Command | Action |
|---|---|
| `hi` / `hello` / `start` | Start / restart the bot |
| `menu` | Return to the main menu |
| `reset` | Clear session and start fresh |
| `stop` | Stop active navigation |
| `help` | Show the help guide |

---

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

=======
---
## 🧪 Running Tests
```bash
mvn test
```
> **Note:** Tests require Java 17. If you are running Java 26, add `-Dnet.bytebuddy.experimental=true` as a JVM argument, or run with `--add-opens` flags to resolve Mockito/ByteBuddy compatibility.
---
## 📌 Useful Commands (in chat)
|
 Command 
|
 Action 
|
|
---
|
---
|
|
`hi`
 / 
`hello`
 / 
`start`
|
 Start / restart the bot 
|
|
`menu`
|
 Return to the main menu 
|
|
`reset`
|
 Clear session and start fresh 
|
|
`stop`
|
 Stop active navigation 
|
|
`help`
|
 Show the help guide 
|
---
## 🤝 Contributing
Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.
---
## 📄 License
This project is open source and available under the [MIT License](LICENSE).
---
>>>>>>> de11572f1fe0fa12a14ba0056275bc247a83cffc
> Built with ❤️ by **Harinath S**
